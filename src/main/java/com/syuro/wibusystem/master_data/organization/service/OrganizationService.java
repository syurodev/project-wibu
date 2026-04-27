package com.syuro.wibusystem.master_data.organization.service;

import com.syuro.wibusystem.master_data.organization.api.OrganizationResponse;
import com.syuro.wibusystem.master_data.organization.dto.CreateOrganizationRequest;
import com.syuro.wibusystem.master_data.organization.dto.UpdateOrganizationRequest;
import com.syuro.wibusystem.master_data.organization.entity.Organization;
import com.syuro.wibusystem.master_data.organization.repository.OrganizationRepository;
import com.syuro.wibusystem.rbac.api.PermissionChecker;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final PermissionChecker permissionChecker;

    @Transactional(readOnly = true)
    public Page<OrganizationResponse> list(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return organizationRepository.findAll(pageable).map(OrganizationResponse::from);
        }
        return organizationRepository.search(q.trim(), pageable).map(OrganizationResponse::from);
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getById(Long id) {
        return OrganizationResponse.from(findById(id));
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getBySlug(String slug) {
        return organizationRepository.findBySlug(slug)
                .map(OrganizationResponse::from)
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_NOT_FOUND));
    }

    @Transactional
    public OrganizationResponse create(Long userId, CreateOrganizationRequest request) {
        requireModerator(userId);
        if (organizationRepository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.ORGANIZATION_SLUG_CONFLICT);
        }
        Organization org = Organization.builder()
                .names(request.getNames())
                .biographies(request.getBiographies() != null ? request.getBiographies() : new HashMap<>())
                .logo(request.getLogo())
                .slug(request.getSlug())
                .build();
        return OrganizationResponse.from(organizationRepository.save(org));
    }

    @Transactional
    public OrganizationResponse update(Long userId, Long id, UpdateOrganizationRequest request) {
        requireModerator(userId);
        Organization org = findById(id);
        if (request.getSlug() != null && !request.getSlug().equals(org.getSlug())) {
            if (organizationRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
                throw new AppException(ErrorCode.ORGANIZATION_SLUG_CONFLICT);
            }
            org.setSlug(request.getSlug());
        }
        if (request.getNames() != null) {
            org.setNames(request.getNames());
        }
        if (request.getBiographies() != null) {
            org.setBiographies(request.getBiographies());
        }
        if (request.getLogo() != null) {
            org.setLogo(request.getLogo());
        }
        return OrganizationResponse.from(organizationRepository.save(org));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        requireModerator(userId);
        Organization org = findById(id);
        org.setDeletedAt(Instant.now());
        org.setDeletedBy(userId);
        organizationRepository.save(org);
    }

    private Organization findById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_NOT_FOUND));
    }

    private void requireModerator(Long userId) {
        if (!permissionChecker.hasPermission(userId, "content:moderate")) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }

}
