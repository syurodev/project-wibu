package com.syuro.wibusystem.security.session.controller.v1;

import com.syuro.wibusystem.security.session.dto.SessionResponse;
import com.syuro.wibusystem.security.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<List<SessionResponse>> list(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Long currentSessionId = (Long) auth.getCredentials();
        return ResponseEntity.ok(sessionService.listActive(userId, currentSessionId));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> removeSession(
            @PathVariable Long sessionId,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        sessionService.removeSession(userId, sessionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeAllSessions(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        sessionService.removeAllSessions(userId);
        return ResponseEntity.noContent().build();
    }
}
