package com.syuro.wibusystem.user.api;

import com.syuro.wibusystem.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountQueryService {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public Optional<CredentialAccount> findCredentialByEmail(String email) {
        return accountRepository.findByProviderEmailAndProvider(email, AccountProvider.CREDENTIAL)
                .map(a -> new CredentialAccount(a.getUserId(), a.getProviderEmail(), a.getPasswordHash()));
    }

    @Transactional(readOnly = true)
    public Optional<CredentialAccount> findCredentialByUserId(Long userId) {
        return accountRepository.findByUserIdAndProvider(userId, AccountProvider.CREDENTIAL)
                .map(a -> new CredentialAccount(a.getUserId(), a.getProviderEmail(), a.getPasswordHash()));
    }
}
