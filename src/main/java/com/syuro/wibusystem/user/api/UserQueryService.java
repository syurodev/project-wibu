package com.syuro.wibusystem.user.api;

import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import com.syuro.wibusystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public UserSummary findById(Long id) {
        return userRepository.findById(id)
                .map(u -> new UserSummary(u.getId(), u.getName(), u.getEmail()))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserProfile findProfileById(Long id) {
        return userRepository.findById(id)
                .map(u -> new UserProfile(u.getId(), u.getName(), u.getAnotherName(), u.getEmail(), u.getAvatar(), u.getSettings()))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public java.util.Optional<UserSummary> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(u -> new UserSummary(u.getId(), u.getName(), u.getEmail()));
    }
}
