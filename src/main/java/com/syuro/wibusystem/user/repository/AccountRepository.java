package com.syuro.wibusystem.user.repository;

import com.syuro.wibusystem.user.api.AccountProvider;
import com.syuro.wibusystem.user.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByProviderEmailAndProvider(String email, AccountProvider provider);

    Optional<Account> findByUserIdAndProvider(Long userId, AccountProvider provider);
}
