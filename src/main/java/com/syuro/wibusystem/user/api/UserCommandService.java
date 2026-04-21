package com.syuro.wibusystem.user.api;

import com.syuro.wibusystem.user.entity.Account;
import com.syuro.wibusystem.user.entity.User;
import com.syuro.wibusystem.user.repository.AccountRepository;
import com.syuro.wibusystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public UserSummary registerCredential(String name, String email, String passwordHash, boolean emailVerified) {
        User user = User.builder()
                .name(name)
                .email(email)
                .isAnonymous(false)
                .settings(new HashMap<>())
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
}
