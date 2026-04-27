package com.syuro.wibusystem.work.service;

import com.syuro.wibusystem.rbac.api.PermissionChecker;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import com.syuro.wibusystem.work.anime.entity.AnimeSeason;
import com.syuro.wibusystem.work.anime.repository.AnimeSeasonRepository;
import com.syuro.wibusystem.work.api.AgeRating;
import com.syuro.wibusystem.work.api.WorkLocalizationStatus;
import com.syuro.wibusystem.work.api.WorkResponse;
import com.syuro.wibusystem.work.api.WorkStatus;
import com.syuro.wibusystem.work.api.WorkType;
import com.syuro.wibusystem.work.dto.CreateWorkRequest;
import com.syuro.wibusystem.work.dto.UpdateWorkRequest;
import com.syuro.wibusystem.work.entity.Work;
import com.syuro.wibusystem.work.entity.WorkGenre;
import com.syuro.wibusystem.work.entity.WorkLocalization;
import com.syuro.wibusystem.work.entity.WorkStaff;
import com.syuro.wibusystem.work.manga.entity.MangaVolume;
import com.syuro.wibusystem.work.manga.repository.MangaVolumeRepository;
import com.syuro.wibusystem.work.novel.entity.NovelVolume;
import com.syuro.wibusystem.work.novel.repository.NovelVolumeRepository;
import com.syuro.wibusystem.work.repository.WorkGenreRepository;
import com.syuro.wibusystem.work.repository.WorkLocalizationRepository;
import com.syuro.wibusystem.work.repository.WorkRepository;
import com.syuro.wibusystem.work.repository.WorkSpec;
import com.syuro.wibusystem.work.repository.WorkStaffRepository;
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
    private final WorkLocalizationRepository workLocalizationRepository;
    private final WorkGenreRepository workGenreRepository;
    private final WorkStaffRepository workStaffRepository;
    private final MangaVolumeRepository mangaVolumeRepository;
    private final NovelVolumeRepository novelVolumeRepository;
    private final AnimeSeasonRepository animeSeasonRepository;
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
        Work saved = workRepository.save(work);

        createOriginalLocalization(saved, userId, creatorProfileId, request);
        attachGenres(saved, request.getGenreIds());
        attachStaffs(saved, request.getStaffs());
        if (saved.isOneshot()) createVirtualContainer(saved);

        return WorkResponse.from(saved);
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
        if (countContainers(work) != 1) throw new AppException(ErrorCode.WORK_TOO_MANY_CHAPTERS_FOR_ONESHOT);
        markFirstContainerVirtual(work);
        work.setOneshot(true);
        return WorkResponse.from(workRepository.save(work));
    }

    // ===== private =====

    private Work findById(Long id) {
        return workRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WORK_NOT_FOUND));
    }

    private long countContainers(Work work) {
        return switch (work.getType()) {
            case ANIME -> animeSeasonRepository.countByWorkId(work.getId());
            case MANGA -> mangaVolumeRepository.countByWorkId(work.getId());
            case NOVEL -> novelVolumeRepository.countByWorkId(work.getId());
        };
    }

    private void createOriginalLocalization(Work work, Long userId, Long creatorProfileId, CreateWorkRequest request) {
        WorkLocalization loc = WorkLocalization.builder()
                .workId(work.getId())
                .language(work.getOriginalLanguage())
                .title(request.getTitle())
                .synopsis(request.getSynopsis() != null ? request.getSynopsis().toString() : null)
                .status(WorkLocalizationStatus.APPROVED)
                .isOriginal(true)
                .submittedBy(creatorProfileId)
                .approvedBy(userId)
                .approvedAt(Instant.now())
                .build();
        workLocalizationRepository.save(loc);
    }

    private void attachGenres(Work work, java.util.List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) return;
        java.util.Set<Long> unique = new java.util.LinkedHashSet<>(genreIds);
        java.util.List<WorkGenre> rows = unique.stream()
                .<WorkGenre>map(gid -> WorkGenre.builder().workId(work.getId()).genreId(gid).build())
                .toList();
        workGenreRepository.saveAll(rows);
    }

    private void attachStaffs(Work work, java.util.List<CreateWorkRequest.StaffEntry> staffs) {
        if (staffs == null || staffs.isEmpty()) return;
        // Loại trùng (peopleId, role)
        java.util.Map<String, CreateWorkRequest.StaffEntry> dedup = new java.util.LinkedHashMap<>();
        for (CreateWorkRequest.StaffEntry s : staffs) {
            dedup.putIfAbsent(s.getPeopleId() + ":" + s.getRole().name(), s);
        }
        java.util.List<WorkStaff> rows = dedup.values().stream()
                .<WorkStaff>map(s -> WorkStaff.builder()
                        .workId(work.getId())
                        .peopleId(s.getPeopleId())
                        .role(s.getRole())
                        .build())
                .toList();
        workStaffRepository.saveAll(rows);
    }

    private void createVirtualContainer(Work work) {
        switch (work.getType()) {
            case MANGA -> mangaVolumeRepository.save(MangaVolume.builder()
                    .workId(work.getId())
                    .volumeNumber(1)
                    .isVirtual(true)
                    .build());
            case NOVEL -> novelVolumeRepository.save(NovelVolume.builder()
                    .workId(work.getId())
                    .volumeNumber(1)
                    .isVirtual(true)
                    .build());
            case ANIME -> animeSeasonRepository.save(AnimeSeason.builder()
                    .workId(work.getId())
                    .seasonNumber(1)
                    .isVirtual(true)
                    .build());
        }
    }

    private void markFirstContainerVirtual(Work work) {
        switch (work.getType()) {
            case MANGA -> mangaVolumeRepository.findFirstByWorkIdOrderByVolumeNumberAsc(work.getId())
                    .ifPresent(v -> { v.setVirtual(true); mangaVolumeRepository.save(v); });
            case NOVEL -> novelVolumeRepository.findFirstByWorkIdOrderByVolumeNumberAsc(work.getId())
                    .ifPresent(v -> { v.setVirtual(true); novelVolumeRepository.save(v); });
            case ANIME -> animeSeasonRepository.findFirstByWorkIdOrderBySeasonNumberAsc(work.getId())
                    .ifPresent(s -> { s.setVirtual(true); animeSeasonRepository.save(s); });
        }
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
