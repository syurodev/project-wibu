package com.syuro.wibusystem.user.controller.v1;

import com.syuro.wibusystem.user.api.UserCommandService;
import com.syuro.wibusystem.user.api.UserQueryService;
import com.syuro.wibusystem.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    @GetMapping("/me/settings")
    public ResponseEntity<Map<String, Object>> getMySettings(
            @AuthenticationPrincipal Long userId) {
        Map<String, Object> settings = userQueryService.findProfileById(userId).settings();
        return ResponseEntity.ok(settings != null ? settings : Map.of());
    }

    @PatchMapping("/me/settings")
    public ResponseEntity<Map<String, Object>> updateMySettings(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateSettingsRequest request) {
        Map<String, Object> updated = userCommandService.updateSettings(userId, request.settings());
        return ResponseEntity.ok(updated);
    }

    public record UpdateSettingsRequest(
            @NotNull Map<String, Object> settings
    ) {}
}
