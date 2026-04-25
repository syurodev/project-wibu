package com.syuro.wibusystem.creator.controller.v1;

import com.syuro.wibusystem.creator.api.CreatorProfileResponse;
import com.syuro.wibusystem.creator.dto.RegisterCreatorRequest;
import com.syuro.wibusystem.creator.service.CreatorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/v1/creator-profiles")
@RequiredArgsConstructor
public class CreatorProfileController {

    private final CreatorProfileService creatorProfileService;

    @GetMapping("/{id}")
    public ResponseEntity<CreatorProfileResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(creatorProfileService.getById(id));
    }

    @PostMapping("/register")
    public ResponseEntity<CreatorProfileResponse> register(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RegisterCreatorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(creatorProfileService.register(userId, request));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<CreatorProfileResponse> restore(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(creatorProfileService.restore(userId, id));
    }
}
