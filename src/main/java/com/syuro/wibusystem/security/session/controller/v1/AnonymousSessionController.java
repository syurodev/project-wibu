package com.syuro.wibusystem.security.session.controller.v1;

import com.syuro.wibusystem.security.session.anonymous.dto.AnonymousSessionResponse;
import com.syuro.wibusystem.security.session.anonymous.dto.DeviceInfo;
import com.syuro.wibusystem.security.session.anonymous.service.AnonymousSessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/sessions")
@RequiredArgsConstructor
public class AnonymousSessionController {

    private final AnonymousSessionService anonymousSessionService;

    /**
     * Tạo hoặc gia hạn anonymous session.
     * Client gọi khi lần đầu truy cập; lưu session_token trả về vào localStorage/cookie.
     * Đính kèm token vào header "X-Session-Token" cho các request tiếp theo.
     * <p>
     * Cùng fingerprint thiết bị → trả lại token cũ (idempotent).
     */
    @PostMapping("/anonymous")
    public ResponseEntity<AnonymousSessionResponse> createAnonymousSession(
            @RequestBody DeviceInfo deviceInfo,
            HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(anonymousSessionService.createOrRenew(deviceInfo, clientIp));
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}
