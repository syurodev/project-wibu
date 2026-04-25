package com.syuro.wibusystem.work.service;

import com.syuro.wibusystem.rbac.api.PermissionChecker;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import com.syuro.wibusystem.work.anime.repository.AnimeEpisodeRepository;
import com.syuro.wibusystem.work.api.AgeRating;
import com.syuro.wibusystem.work.api.WorkResponse;
import com.syuro.wibusystem.work.api.WorkStatus;
import com.syuro.wibusystem.work.api.WorkType;
import com.syuro.wibusystem.work.dto.CreateWorkRequest;
import com.syuro.wibusystem.work.dto.UpdateWorkRequest;
import com.syuro.wibusystem.work.entity.Work;
import com.syuro.wibusystem.work.manga.repository.MangaChapterRepository;
import com.syuro.wibusystem.work.novel.repository.NovelChapterRepository;
import com.syuro.wibusystem.work.repository.WorkRepository;
import com.syuro.wibusystem.work.repository.WorkSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class WorkService {

    private final WorkRepository workRepository;
    private final AnimeEpisodeRepository animeEpisodeRepository;
    private final MangaChapterRepository mangaChapterRepository;
    private final NovelChapterRepository novelChapterRepository;
    private final PermissionChecker permissionChecker;

    @Transactional(readOnly = true)
    public Page<WorkResponse> list(WorkType type, WorkStatus status, AgeRating ageRating, Pageable pageable) {
        Specification<Work> spec = Specification
                .where(WorkSpec.byType(type))
                .and(WorkSpec.byStatus(status))
                .and(WorkSpec.byAgeRating(ageRating));
        return workRepository.findAll(spec, pageable).map(WorkResponse::from);
    }

    @Transactional(readOnly = true)
    public WorkResponse getById(Long id) {
        return WorkResponse.from(findById(id));
    }

    @Transactional
    public WorkResponse create(Long userId, Long creatorProfileId, CreateWorkRequest request) {
        requireCanCreate(userId);
        if (creatorProfileId == null) throw new AppException(ErrorCode.FORBIDDEN);
        Work work = Work.builder()
                .orgId(request.getOrgId())
                .creatorProfileId(creatorProfileId)
                .type(request.getType())
                .status(request.getStatus() != null ? request.getStatus() : WorkStatus.ON_GOING)
                .originalLanguage(request.getOriginalLanguage())
                .cover(request.getCover())
                .airedFrom(request.getAiredFrom())
                .airedTo(request.getAiredTo())
                .ageRating(request.getAgeRating())
                .isOneshot(request.isOneshot())
                .build();
        return WorkResponse.from(workRepository.save(work));
    }

    @Transactional
    public WorkResponse update(Long userId, Long creatorProfileId, Long id, UpdateWorkRequest request) {
        Work work = findById(id);
        requireCanEdit(userId, creatorProfileId, work);
        if (request.getStatus() != null) work.setStatus(request.getStatus());
        if (request.getOriginalLanguage() != null) work.setOriginalLanguage(request.getOriginalLanguage());
        if (request.getCover() != null) work.setCover(request.getCover());
        if (request.getAiredFrom() != null) work.setAiredFrom(request.getAiredFrom());
        if (request.getAiredTo() != null) work.setAiredTo(request.getAiredTo());
        if (request.getAgeRating() != null) work.setAgeRating(request.getAgeRating());
        return WorkResponse.from(workRepository.save(work));
    }

    @Transactional
    public void delete(Long userId, Long creatorProfileId, Long id) {
        Work work = findById(id);
        requireCanDelete(userId, creatorProfileId, work);
        work.setDeletedAt(Instant.now());
        work.setDeletedBy(userId);
        workRepository.save(work);
    }

    @Transactional
    public WorkResponse convertToSeries(Long userId, Long creatorProfileId, Long id) {
        Work work = findById(id);
        requireCanEdit(userId, creatorProfileId, work);
        if (!work.isOneshot()) throw new AppException(ErrorCode.WORK_ALREADY_SERIES);
        work.setOneshot(false);
        return WorkResponse.from(workRepository.save(work));
    }

    @Transactional
    public WorkResponse convertToOneshot(Long userId, Long creatorProfileId, Long id) {
        Work work = findById(id);
        requireCanEdit(userId, creatorProfileId, work);
        if (work.isOneshot()) throw new AppException(ErrorCode.WORK_ALREADY_ONESHOT);
        if (countContent(work) > 1) throw new AppException(ErrorCode.WORK_TOO_MANY_CHAPTERS_FOR_ONESHOT);
        work.setOneshot(true);
        return WorkResponse.from(workRepository.save(work));
    }

    // ===== private =====

    private Work findById(Long id) {
        return workRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WORK_NOT_FOUND));
    }

    private long countContent(Work work) {
        return switch (work.getType()) {
            case ANIME -> animeEpisodeRepository.countByWorkId(work.getId());
            case MANGA -> mangaChapterRepository.countByWorkId(work.getId());
            case NOVEL -> novelChapterRepository.countByWorkId(work.getId());
        };
    }

    private void requireCanCreate(Long userId) {
        permissionChecker.requirePermission(userId, "content:create");
    }

    private void requireCanEdit(Long userId, Long creatorProfileId, Work work) {
        if (permissionChecker.hasAnyRole(userId, "admin", "super_admin")) return;
        if (creatorProfileId != null
                && creatorProfileId.equals(work.getCreatorProfileId())
                && permissionChecker.hasPermission(userId, "content:edit_own")) return;
        throw new AppException(ErrorCode.FORBIDDEN);
    }

    private void requireCanDelete(Long userId, Long creatorProfileId, Work work) {
        if (permissionChecker.hasPermission(userId, "content:delete_any")) return;
        if (creatorProfileId != null
                && creatorProfileId.equals(work.getCreatorProfileId())
                && permissionChecker.hasPermission(userId, "content:delete_own")) return;
        throw new AppException(ErrorCode.FORBIDDEN);
    }
}
