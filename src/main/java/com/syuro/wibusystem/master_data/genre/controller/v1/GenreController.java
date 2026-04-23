package com.syuro.wibusystem.master_data.genre.controller.v1;

import com.syuro.wibusystem.master_data.genre.api.GenreResponse;
import com.syuro.wibusystem.master_data.genre.dto.CreateGenreRequest;
import com.syuro.wibusystem.master_data.genre.dto.UpdateGenreRequest;
import com.syuro.wibusystem.master_data.genre.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public ResponseEntity<Page<GenreResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(genreService.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(genreService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<GenreResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(genreService.getBySlug(slug));
    }

    @PostMapping
    public ResponseEntity<GenreResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateGenreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(genreService.create(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenreResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateGenreRequest request) {
        return ResponseEntity.ok(genreService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        genreService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
