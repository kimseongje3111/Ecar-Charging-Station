package com.ecar.servicestation.modules.user.repository;

import com.ecar.servicestation.modules.user.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Account, Long> {

    boolean existsAccountByEmail(String email);

    Optional<Account> findAccountByEmail(String email);
}
