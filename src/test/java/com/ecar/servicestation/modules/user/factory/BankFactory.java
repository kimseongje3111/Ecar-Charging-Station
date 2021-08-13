package com.ecar.servicestation.modules.user.factory;

import com.ecar.servicestation.infra.bank.service.BankService;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Bank;
import com.ecar.servicestation.modules.user.exception.users.CUserNotFoundException;
import com.ecar.servicestation.modules.user.repository.BankRepository;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BankFactory {

    private final UserRepository userRepository;
    private final BankRepository bankRepository;
    private final BankService bankService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Bank createBank(String bankName, String accountNumber, Account account) {
        account = userRepository.findAccountByEmail(account.getEmail()).orElseThrow(CUserNotFoundException::new);

        Bank bank =
                bankRepository.save(
                        Bank.builder()
                                .bankName(bankName)
                                .bankAccountNumber(accountNumber)
                                .bankAccountOwner("ADMIN01")
                                .build()
                );

        bank.generateAuthMsg();
        bank.setBankAccountAccessToken(
                bankService.bankAccountUserAuthentication(
                        bank.getBankName(),
                        bank.getBankAccountNumber(),
                        1L,
                        "ADMIN01-CERTIFICATE-PASSWORD"
                )
        );

        account.addBank(bank);

        return bank;
    }

    @Transactional
    public Bank createVerifiedBank(String bankName, String accountNumber, Account account) {
        Bank bank = createBank(bankName, accountNumber, account);

        bank.successBankAccountAuthentication();
        bank.setPaymentPassword(passwordEncoder.encode(bankName + "-PAYMENT-PASSWORD"));

        return bank;
    }

}
