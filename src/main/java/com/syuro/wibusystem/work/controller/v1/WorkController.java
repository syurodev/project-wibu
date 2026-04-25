package com.syuro.wibusystem.work.controller.v1;

import com.syuro.wibusystem.security.session.api.SessionCachePayload;
import com.syuro.wibusystem.work.api.AgeRating;
import com.syuro.wibusystem.work.api.WorkResponse;
import com.syuro.wibusystem.work.api.WorkStatus;
import com.syuro.wibusystem.work.api.WorkType;
import com.syuro.wibusystem.work.dto.CreateWorkRequest;
import com.syuro.wibusystem.work.dto.UpdateWorkRequest;
import com.syuro.wibusystem.work.service.WorkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/works")
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;

    @GetMapping
    public ResponseEntity<Page<WorkResponse>> list(
            @RequestParam(required = false) WorkType type,
            @RequestParam(required = false) WorkStatus status,
            @RequestParam(required = false) AgeRating ageRating,
            Pageable pageable) {
        return ResponseEntity.ok(workService.list(type, status, ageRating, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(workService.getById(id));
    }

    @PostMapping
    public ResponseEntity<WorkResponse> create(
            @AuthenticationPrincipal Long userId,
            Authentication auth,
            @Valid @RequestBody CreateWorkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workService.create(userId, creatorProfileId(auth), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkResponse> update(
            @AuthenticationPrincipal Long userId,
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkRequest request) {
        return ResponseEntity.ok(workService.update(userId, creatorProfileId(auth), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            Authentication auth,
            @PathVariable Long id) {
        workService.delete(userId, creatorProfileId(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/to-series")
    public ResponseEntity<WorkResponse> convertToSeries(
            @AuthenticationPrincipal Long userId,
            Authentication auth,
            @PathVariable Long id) {
        return ResponseEntity.ok(workService.convertToSeries(userId, creatorProfileId(auth), id));
    }

    @PatchMapping("/{id}/to-oneshot")
    public ResponseEntity<WorkResponse> convertToOneshot(
            @AuthenticationPrincipal Long userId,
            Authentication auth,
            @PathVariable Long id) {
        return ResponseEntity.ok(workService.convertToOneshot(userId, creatorProfileId(auth), id));
    }

    private Long creatorProfileId(Authentication auth) {
        return ((SessionCachePayload) auth.getCredentials()).creatorProfileId();
    }
}
