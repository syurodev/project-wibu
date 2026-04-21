package com.syuro.wibusystem.security.auth.service;

import com.syuro.wibusystem.mail.api.MailService;
import com.syuro.wibusystem.rbac.api.GlobalRoleName;
import com.syuro.wibusystem.rbac.api.PermissionChecker;
import com.syuro.wibusystem.rbac.api.RbacCommandService;
import com.syuro.wibusystem.security.auth.dto.*;
import com.syuro.wibusystem.security.session.entity.Session;
import com.syuro.wibusystem.security.session.repository.SessionRepository;
import com.syuro.wibusystem.security.jwt.JwtProperties;
import com.syuro.wibusystem.security.jwt.JwtService;
import com.syuro.wibusystem.security.jwt.TokenBlacklistService;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import com.syuro.wibusystem.shared.id.SnowflakeGenerator;
import com.syuro.wibusystem.user.api.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
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
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final OtpService otpService;
    private final MailService mailService;
    private final MagicLinkTokenService magicLinkTokenService;

    public RegisterResponse register(RegisterRequest request) {
        if (userQueryService.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_IN_USE);
        }

        String userId = String.valueOf(SnowflakeGenerator.nextId());
        String otp = otpService.generateAndStore(
                userId,
                request.getEmail(),
                request.getName(),
                passwordEncoder.encode(request.getPassword())
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
                true
        );

        rbacCommandService.assignGlobalRole(user.id(), GlobalRoleName.USER.value());

        return new VerifyOtpResponse(user.id(), user.name(), user.email());
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request, String userAgent, String ipAddress) {
        CredentialAccount account = resolveCredentialAccount(request.getIdentifier());

        if (!passwordEncoder.matches(request.getPassword(), account.passwordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        UserProfile user = userQueryService.findProfileById(account.userId());
        List<String> roles = permissionChecker.getGlobalRoleNames(user.id());
        List<String> permissions = permissionChecker.getExpandedPermissions(user.id());

        String rawRefreshToken = generateRefreshToken();
        Session session = sessionRepository.save(Session.builder()
                .userId(user.id())
                .refreshTokenHash(hashToken(rawRefreshToken))
                .deviceUserAgent(userAgent)
                .ipAddress(ipAddress)
                .expiresAt(Instant.now().plusSeconds(jwtProperties.refreshTokenExpiry()))
                .build());

        String accessToken = jwtService.generateAccessToken(
                user.id(), session.getId(), user.email(), user.name(), permissions);

        return new LoginResponse(accessToken, rawRefreshToken, jwtProperties.accessTokenExpiry(), user, roles, permissions);
    }

    @Transactional(rollbackFor = Exception.class)
    public RefreshResponse refresh(RefreshRequest request, String userAgent, String ipAddress) {
        String hash = hashToken(request.getRefreshToken());

        Session old = sessionRepository.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID));

        if (old.getRevokedAt() != null || old.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        old.setRevokedAt(Instant.now());
        tokenBlacklistService.blacklist(old.getId(), Duration.ofSeconds(jwtProperties.accessTokenExpiry()));

        UserSummary user = userQueryService.findById(old.getUserId());
        List<String> permissions = permissionChecker.getExpandedPermissions(user.id());

        String rawRefreshToken = generateRefreshToken();
        Session newSession = sessionRepository.save(Session.builder()
                .userId(user.id())
                .refreshTokenHash(hashToken(rawRefreshToken))
                .deviceUserAgent(userAgent)
                .ipAddress(ipAddress)
                .expiresAt(Instant.now().plusSeconds(jwtProperties.refreshTokenExpiry()))
                .build());

        String accessToken = jwtService.generateAccessToken(
                user.id(), newSession.getId(), user.email(), user.name(), permissions);

        return new RefreshResponse(accessToken, rawRefreshToken, jwtProperties.accessTokenExpiry());
    }

    public MagicLinkSendResponse sendMagicLink(MagicLinkSendRequest request) {
        accountQueryService.findCredentialByEmail(request.getEmail()).ifPresent(account -> {
            UserProfile user = userQueryService.findProfileById(account.userId());
            String token = generateRefreshToken();
            magicLinkTokenService.store(token, user.id(), user.email());
            mailService.sendMagicLinkEmail(user.email(), user.name(), request.getCallbackUrl() + "?token=" + token);
        });
        return new MagicLinkSendResponse("Nếu email của bạn đã đăng ký, bạn sẽ nhận được link đăng nhập.");
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginResponse verifyMagicLink(MagicLinkVerifyRequest request, String userAgent, String ipAddress) {
        MagicLinkPending pending = magicLinkTokenService.verifyAndConsume(request.getToken());

        UserProfile user = userQueryService.findProfileById(pending.userId());
        List<String> roles = permissionChecker.getGlobalRoleNames(user.id());
        List<String> permissions = permissionChecker.getExpandedPermissions(user.id());

        String rawRefreshToken = generateRefreshToken();
        Session session = sessionRepository.save(Session.builder()
                .userId(user.id())
                .refreshTokenHash(hashToken(rawRefreshToken))
                .deviceUserAgent(userAgent)
                .ipAddress(ipAddress)
                .expiresAt(Instant.now().plusSeconds(jwtProperties.refreshTokenExpiry()))
                .build());

        String accessToken = jwtService.generateAccessToken(
                user.id(), session.getId(), user.email(), user.name(), permissions);

        return new LoginResponse(accessToken, rawRefreshToken, jwtProperties.accessTokenExpiry(), user, roles, permissions);
    }

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

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
