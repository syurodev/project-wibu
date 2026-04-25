package com.syuro.wibusystem.creator.service;

import com.syuro.wibusystem.creator.api.CreatorProfileResponse;
import com.syuro.wibusystem.creator.api.CreatorStatus;
import com.syuro.wibusystem.creator.dto.RegisterCreatorRequest;
import com.syuro.wibusystem.creator.entity.CreatorProfile;
import com.syuro.wibusystem.creator.repository.CreatorProfileRepository;
import com.syuro.wibusystem.rbac.api.PermissionChecker;
import com.syuro.wibusystem.rbac.api.RbacCommandService;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class CreatorProfileService {

    private final CreatorProfileRepository creatorProfileRepository;
    private final RbacCommandService rbacCommandService;
    private final PermissionChecker permissionChecker;

    @Transactional(readOnly = true)
    public CreatorProfileResponse getById(Long id) {
        return CreatorProfileResponse.from(
                creatorProfileRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.CREATOR_PROFILE_NOT_FOUND))
        );
    }

    /**
     * Đăng ký trở thành creator.
     * Mỗi user chỉ được có 1 profile kể cả khi đã bị xóa — admin mới restore được.
     */
    @Transactional
    public CreatorProfileResponse register(Long userId, RegisterCreatorRequest request) {
        // Kiểm tra cả profile đã soft-delete — CreatorProfile không có @SQLRestriction
        // nên findByUserId trả về bất kỳ record nào của userId này
        if (creatorProfileRepository.findByUserId(userId).isPresent()) {
            throw new AppException(ErrorCode.CREATOR_PROFILE_ALREADY_EXISTS);
        }
        if (creatorProfileRepository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.CREATOR_PROFILE_SLUG_CONFLICT);
        }

        CreatorProfile profile = CreatorProfile.builder()
                .userId(userId)
                .stageName(request.getStageName())
                .slug(request.getSlug())
                .avatar(request.getAvatar())
                .banner(request.getBanner())
                .bio(request.getBio() != null ? request.getBio() : new HashMap<>())
                .build();

        profile = creatorProfileRepository.save(profile);
        rbacCommandService.assignGlobalRole(userId, "creator");
        return CreatorProfileResponse.from(profile);
    }

    /**
     * Khôi phục profile đã bị xóa mềm.
     * Chỉ admin và super_admin mới có quyền.
     */
    @Transactional
    public CreatorProfileResponse restore(Long adminUserId, Long profileId) {
        permissionChecker.requireAnyRole(adminUserId, "admin", "super_admin");

        CreatorProfile profile = creatorProfileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.CREATOR_PROFILE_NOT_FOUND));

        if (profile.getDeletedAt() == null) {
            throw new AppException(ErrorCode.CREATOR_PROFILE_NOT_DELETED);
        }

        profile.setDeletedAt(null);
        profile.setDeletedBy(null);
        profile.setStatus(CreatorStatus.ACTIVE);
        profile = creatorProfileRepository.save(profile);

        rbacCommandService.assignGlobalRole(profile.getUserId(), "creator");
        return CreatorProfileResponse.from(profile);
    }

}
