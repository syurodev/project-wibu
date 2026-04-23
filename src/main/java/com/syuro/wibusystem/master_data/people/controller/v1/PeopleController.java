package com.syuro.wibusystem.master_data.people.controller.v1;

import com.syuro.wibusystem.master_data.people.api.PeopleResponse;
import com.syuro.wibusystem.master_data.people.dto.CreatePeopleRequest;
import com.syuro.wibusystem.master_data.people.dto.UpdatePeopleRequest;
import com.syuro.wibusystem.master_data.people.service.PeopleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/peoples")
@RequiredArgsConstructor
public class PeopleController {

    private final PeopleService peopleService;

    @GetMapping
    public ResponseEntity<Page<PeopleResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(peopleService.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PeopleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(peopleService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<PeopleResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(peopleService.getBySlug(slug));
    }

    @PostMapping
    public ResponseEntity<PeopleResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreatePeopleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(peopleService.create(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PeopleResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePeopleRequest request) {
        return ResponseEntity.ok(peopleService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        peopleService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
