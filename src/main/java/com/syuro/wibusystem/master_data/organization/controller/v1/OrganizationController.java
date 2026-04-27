package com.syuro.wibusystem.master_data.organization.controller.v1;

import com.syuro.wibusystem.master_data.organization.api.OrganizationResponse;
import com.syuro.wibusystem.master_data.organization.dto.CreateOrganizationRequest;
import com.syuro.wibusystem.master_data.organization.dto.UpdateOrganizationRequest;
import com.syuro.wibusystem.master_data.organization.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    public ResponseEntity<Page<OrganizationResponse>> list(
            @RequestParam(required = false) String q,
            Pageable pageable) {
        return ResponseEntity.ok(organizationService.list(q, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(organizationService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<OrganizationResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(organizationService.getBySlug(slug));
    }

    @PostMapping
    public ResponseEntity<OrganizationResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateOrganizationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationService.create(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        return ResponseEntity.ok(organizationService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        organizationService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
