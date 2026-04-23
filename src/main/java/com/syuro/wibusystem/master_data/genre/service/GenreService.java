package com.syuro.wibusystem.master_data.genre.service;

import com.syuro.wibusystem.master_data.genre.api.GenreResponse;
import com.syuro.wibusystem.master_data.genre.dto.CreateGenreRequest;
import com.syuro.wibusystem.master_data.genre.dto.UpdateGenreRequest;
import com.syuro.wibusystem.master_data.genre.entity.Genre;
import com.syuro.wibusystem.master_data.genre.repository.GenreRepository;
import com.syuro.wibusystem.rbac.api.PermissionChecker;
import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;
    private final PermissionChecker permissionChecker;

    @Transactional(readOnly = true)
    public Page<GenreResponse> list(Pageable pageable) {
        return genreRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public GenreResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public GenreResponse getBySlug(String slug) {
        return genreRepository.findBySlug(slug)
                .map(this::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND));
    }

    @Transactional
    public GenreResponse create(Long userId, CreateGenreRequest request) {
        requireModerator(userId);
        if (genreRepository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.GENRE_SLUG_CONFLICT);
        }
        Genre genre = Genre.builder()
                .titles(request.getTitles())
                .slug(request.getSlug())
                .build();
        return toResponse(genreRepository.save(genre));
    }

    @Transactional
    public GenreResponse update(Long userId, Long id, UpdateGenreRequest request) {
        requireModerator(userId);
        Genre genre = findById(id);
        if (request.getSlug() != null && !request.getSlug().equals(genre.getSlug())) {
            if (genreRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
                throw new AppException(ErrorCode.GENRE_SLUG_CONFLICT);
            }
            genre.setSlug(request.getSlug());
        }
        if (request.getTitles() != null) {
            genre.setTitles(request.getTitles());
        }
        return toResponse(genreRepository.save(genre));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        requireModerator(userId);
        Genre genre = findById(id);
        genre.setDeletedAt(Instant.now());
        genre.setDeletedBy(userId);
        genreRepository.save(genre);
    }

    private Genre findById(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND));
    }

    private void requireModerator(Long userId) {
        if (!permissionChecker.hasPermission(userId, "content:moderate")) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }

    private GenreResponse toResponse(Genre genre) {
        return new GenreResponse(
                genre.getId(),
                genre.getTitles(),
                genre.getSlug(),
                genre.getViewCount(),
                genre.getCreatedAt(),
                genre.getUpdatedAt()
        );
    }
}
