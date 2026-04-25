package com.syuro.wibusystem.user.api;

import com.syuro.wibusystem.shared.exception.AppException;
import com.syuro.wibusystem.shared.exception.ErrorCode;
import com.syuro.wibusystem.user.entity.Account;
import com.syuro.wibusystem.user.entity.User;
import com.syuro.wibusystem.user.repository.AccountRepository;
import com.syuro.wibusystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public UserSummary registerCredential(String name, String email, String passwordHash,
                                          String language, boolean emailVerified) {
        User user = User.builder()
                .name(name)
                .email(email)
                .isAnonymous(false)
                .settings(defaultSettings(language))
                .build();
        user = userRepository.save(user);

        Account account = Account.builder()
                .userId(user.getId())
                .provider(AccountProvider.CREDENTIAL)
                .providerEmail(email)
                .passwordHash(passwordHash)
                .emailVerified(emailVerified)
                .build();
        accountRepository.save(account);

        return new UserSummary(user.getId(), user.getName(), user.getEmail());
    }

    @Transactional
    public Map<String, Object> updateSettings(Long userId, Map<String, Object> patch) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Map<String, Object> merged = deepMerge(user.getSettings(), patch);
        user.setSettings(merged);
        userRepository.save(user);
        return merged;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> deepMerge(Map<String, Object> base, Map<String, Object> patch) {
        Map<String, Object> result = new HashMap<>(base);
        patch.forEach((key, value) -> {
            if (value instanceof Map && result.get(key) instanceof Map) {
                result.put(key, deepMerge(
                        (Map<String, Object>) result.get(key),
                        (Map<String, Object>) value
                ));
            } else {
                result.put(key, value);
            }
        });
        return result;
    }

    private static Map<String, Object> defaultSettings(String language) {
        return Map.of(
                "language", language,
                "notifications_enabled", true,
                "content_filters", List.of(),
                "ui_preferences", Map.of(
                        "theme", "system"
                ),
                "watch_settings", Map.of(
                        "auto_play_video", true,
                        "show_mature_content", false
                ),
                "reading_settings", Map.of(
                        "font_family", "serif",
                        "font_size", 16,
                        "text_align", "left",
                        "reading_theme", "group_system"
                )
        );
    }
}
