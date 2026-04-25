package com.syuro.wibusystem.creator.api;

import com.syuro.wibusystem.creator.repository.CreatorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatorQueryService {

    private final CreatorProfileRepository creatorProfileRepository;

    @Transactional(readOnly = true)
    public Long findIdByUserId(Long userId) {
        return creatorProfileRepository.findByUserId(userId)
                .map(cp -> cp.getId())
                .orElse(null);
    }
}
