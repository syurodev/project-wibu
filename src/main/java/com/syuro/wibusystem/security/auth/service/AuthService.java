package com.syuro.wibusystem.security.auth.service;

import com.syuro.wibusystem.creator.api.CreatorQueryService;
import com.syuro.wibusystem.mail.api.MailService;
import com.syuro.wibusystem.rbac.api.GlobalRoleName;
import com.syuro.wibusystem.rbac.api.PermissionChecker;
import com.syuro.wibusystem.rbac.api.RbacCommandService;
import com.syuro.wibusystem.security.auth.dto.*;
import com.syuro.wibusystem.security.rsa.RsaKeyService;
import com.syuro.wibusystem.security.session.api.SessionCachePayload;
import com.syuro.wibusystem.security.session.config.SessionProperties;
import com.syuro.wibusystem.security.session.entity.Session;
import com.syuro.wibusystem.security.session.repository.SessionRepository;
import com.syuro.wibusystem.security.session.service.SessionExtendService;
import com.syuro.wibusystem.security.session.service.SessionTokenService;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import com.syuro.wibusystem.shared.id.SnowflakeGenerator;
import com.syuro.wibusystem.user.api.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final AccountQueryService accountQueryService;
    private final RbacCommandService rbacCommandService;
    private final PermissionChecker permissionChecker;
    private final SessionRepository sessionRepository;
    private final SessionTokenService tokenService;
    private final SessionProperties sessionProps;
    private final SessionExtendService sessionExtendService;
    private final RsaKeyService rsaKeyService;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final MailService mailService;
    private final MagicLinkTokenService magicLinkTokenService;
    private final CreatorQueryService creatorQueryService;

    public RegisterResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        if (userQueryService.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_IN_USE);
        }

        String userId = String.valueOf(SnowflakeGenerator.nextId());
        String language = resolveLanguage(httpRequest.getHeader("Accept-Language"));
        String plainPassword = rsaKeyService.decrypt(request.getPassword());
        String otp = otpService.generateAndStore(
                userId,
                request.getEmail(),
                request.getName(),
                passwordEncoder.encode(plainPassword),
                language
        );

        mailService.sendOtpEmail(request.getEmail(), request.getName(), otp);

        return new RegisterResponse(Long.parseLong(userId), request.getEmail());
    }

    @Transactional(rollbackFor = Exception.class)
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        PendingRegistration pending = otpService.verifyAndConsume(request.getUserId().strip(), request.getOtp());

        if (userQueryService.existsByEmail(pending.email())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_IN_USE);
        }

        UserSummary user = userCommandService.registerCredential(
                pending.name(),
                pending.email(),
                pending.passwordHash(),
                pending.language(),
                true
        );

        rbacCommandService.assignGlobalRole(user.id(), GlobalRoleName.USER.value());

        return new VerifyOtpResponse(user.id(), user.name(), user.email());
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request, String userAgent, String ipAddress) {
        CredentialAccount account = resolveCredentialAccount(request.getIdentifier());

        String plainPassword = rsaKeyService.decrypt(request.getPassword());
        if (!passwordEncoder.matches(plainPassword, account.passwordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        return createSession(account.userId(), userAgent, ipAddress);
    }

    public MagicLinkSendResponse sendMagicLink(MagicLinkSendRequest request) {
        accountQueryService.findCredentialByEmail(request.getEmail()).ifPresent(account -> {
            UserProfile user = userQueryService.findProfileById(account.userId());
            String token = tokenService.generateRawToken();
            magicLinkTokenService.store(token, user.id(), user.email());
            mailService.sendMagicLinkEmail(user.email(), user.name(), request.getCallbackUrl() + "?token=" + token);
        });
        return new MagicLinkSendResponse("Nếu email của bạn đã đăng ký, bạn sẽ nhận được link đăng nhập.");
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginResponse verifyMagicLink(MagicLinkVerifyRequest request, String userAgent, String ipAddress) {
        MagicLinkPending pending = magicLinkTokenService.verifyAndConsume(request.getToken());
        return createSession(pending.userId(), userAgent, ipAddress);
    }

    public SessionResponse getSessionInfo(String signedToken) {
        String rawToken = tokenService.verifyToken(signedToken);
        if (rawToken == null) throw new AppException(ErrorCode.SESSION_NOT_FOUND);

        String tokenHash = sha256(rawToken);
        Session session = sessionRepository.findByRefreshTokenHash(tokenHash)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getRevokedAt() != null || session.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.SESSION_NOT_FOUND);
        }

        sessionExtendService.extendIfNeeded(session);

        SessionCachePayload payload = sessionExtendService.buildPayload(session);
        String newSessionData = tokenService.signSessionData(payload);

        UserProfile user = userQueryService.findProfileById(session.getUserId());
        long expiresIn = Instant.now().until(session.getExpiresAt(), java.time.temporal.ChronoUnit.SECONDS);

        return new SessionResponse(
                newSessionData,
                signedToken,
                expiresIn,
                user,
                payload.roles(),
                payload.permissions(),
                payload.creatorProfileId()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void logout(String signedToken) {
        String rawToken = tokenService.verifyToken(signedToken);
        if (rawToken == null) return;

        String tokenHash = sha256(rawToken);
        sessionRepository.findByRefreshTokenHash(tokenHash).ifPresent(session -> {
            session.setRevokedAt(Instant.now());
            sessionRepository.save(session);
        });
    }

    // ─── Shared session creation ───────────────────────────────────────────────

    public LoginResponse createSession(Long userId, String userAgent, String ipAddress) {
        UserProfile user = userQueryService.findProfileById(userId);
        List<String> roles = permissionChecker.getGlobalRoleNames(userId);
        List<String> permissions = permissionChecker.getExpandedPermissions(userId);

        String rawToken = tokenService.generateRawToken();
        String tokenHash = sha256(rawToken);

        Session session = sessionRepository.save(Session.builder()
                .userId(userId)
                .refreshTokenHash(tokenHash)
                .deviceUserAgent(userAgent)
                .ipAddress(ipAddress)
                .expiresAt(Instant.now().plusSeconds(sessionProps.expiresIn()))
                .build());

        String signedToken = tokenService.signToken(rawToken);

        Long creatorProfileId = creatorQueryService.findIdByUserId(userId);

        String version = String.valueOf(session.getUpdatedAt() != null
                ? session.getUpdatedAt().toEpochMilli()
                : session.getCreatedAt().toEpochMilli());
        SessionCachePayload cachePayload = new SessionCachePayload(
                userId, user.email(), user.name(), roles, permissions, version, 0L, creatorProfileId);
        String sessionData = tokenService.signSessionData(cachePayload);

        return new LoginResponse(signedToken, sessionData, sessionProps.expiresIn(), user, roles, permissions, creatorProfileId);
    }

    // ─── Utilities ─────────────────────────────────────────────────────────────

    private CredentialAccount resolveCredentialAccount(String identifier) {
        if (identifier.contains("@")) {
            return accountQueryService.findCredentialByEmail(identifier)
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));
        }
        UserSummary user = userQueryService.findByUsername(identifier)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));
        return accountQueryService.findCredentialByUserId(user.id())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));
    }

    /**
     * Parse Accept-Language header — chỉ hỗ trợ "en" và "vi", còn lại mặc định "en"
     */
    private String resolveLanguage(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) return "en";
        String primary = acceptLanguage.split("[,;]")[0].trim().toLowerCase();
        return primary.startsWith("vi") ? "vi" : "en";
    }

    private String sha256(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
