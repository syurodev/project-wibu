package com.syuro.wibusystem.master_data.people.service;

import com.syuro.wibusystem.master_data.people.api.PeopleResponse;
import com.syuro.wibusystem.master_data.people.dto.CreatePeopleRequest;
import com.syuro.wibusystem.master_data.people.dto.UpdatePeopleRequest;
import com.syuro.wibusystem.master_data.people.entity.People;
import com.syuro.wibusystem.master_data.people.repository.PeopleRepository;
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
public class PeopleService {

    private final PeopleRepository peopleRepository;
    private final PermissionChecker permissionChecker;

    @Transactional(readOnly = true)
    public Page<PeopleResponse> list(Pageable pageable) {
        return peopleRepository.findAll(pageable).map(PeopleResponse::from);
    }

    @Transactional(readOnly = true)
    public PeopleResponse getById(Long id) {
        return PeopleResponse.from(findById(id));
    }

    @Transactional(readOnly = true)
    public PeopleResponse getBySlug(String slug) {
        return peopleRepository.findBySlug(slug)
                .map(PeopleResponse::from)
                .orElseThrow(() -> new AppException(ErrorCode.PEOPLE_NOT_FOUND));
    }

    @Transactional
    public PeopleResponse create(Long userId, CreatePeopleRequest request) {
        requireModerator(userId);
        if (peopleRepository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.PEOPLE_SLUG_CONFLICT);
        }
        People people = People.builder()
                .names(request.getNames())
                .biographies(request.getBiographies() != null ? request.getBiographies() : new HashMap<>())
                .avatar(request.getAvatar())
                .birthday(request.getBirthday())
                .slug(request.getSlug())
                .build();
        return PeopleResponse.from(peopleRepository.save(people));
    }

    @Transactional
    public PeopleResponse update(Long userId, Long id, UpdatePeopleRequest request) {
        requireModerator(userId);
        People people = findById(id);
        if (request.getSlug() != null && !request.getSlug().equals(people.getSlug())) {
            if (peopleRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
                throw new AppException(ErrorCode.PEOPLE_SLUG_CONFLICT);
            }
            people.setSlug(request.getSlug());
        }
        if (request.getNames() != null) {
            people.setNames(request.getNames());
        }
        if (request.getBiographies() != null) {
            people.setBiographies(request.getBiographies());
        }
        if (request.getAvatar() != null) {
            people.setAvatar(request.getAvatar());
        }
        if (request.getBirthday() != null) {
            people.setBirthday(request.getBirthday());
        }
        return PeopleResponse.from(peopleRepository.save(people));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        requireModerator(userId);
        People people = findById(id);
        people.setDeletedAt(Instant.now());
        people.setDeletedBy(userId);
        peopleRepository.save(people);
    }

    private People findById(Long id) {
        return peopleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PEOPLE_NOT_FOUND));
    }

    private void requireModerator(Long userId) {
        if (!permissionChecker.hasPermission(userId, "content:moderate")) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }

}
