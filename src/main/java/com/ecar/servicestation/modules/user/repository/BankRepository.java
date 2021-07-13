package com.ecar.servicestation.modules.user.repository;

import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankRepository extends JpaRepository<Bank, Long> {

    Bank findBankByIdAndAccount(long bankId, Account account);

    List<Bank> findAllByAccount(Account account);

}
