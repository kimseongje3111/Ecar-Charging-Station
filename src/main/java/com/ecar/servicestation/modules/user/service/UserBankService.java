package com.ecar.servicestation.modules.user.service;

import com.ecar.servicestation.infra.bank.BankService;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Bank;
import com.ecar.servicestation.modules.user.dto.request.ConfirmBankRequest;
import com.ecar.servicestation.modules.user.dto.request.RegisterBankRequest;
import com.ecar.servicestation.modules.user.dto.response.RegisterBankResponse;
import com.ecar.servicestation.modules.user.exception.CBankAuthFailedException;
import com.ecar.servicestation.modules.user.exception.CBankNotFoundException;
import com.ecar.servicestation.modules.user.exception.CUserNotFoundException;
import com.ecar.servicestation.modules.user.repository.BankRepository;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBankService {

    private final BankRepository bankRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final BankService bankService;

    @Transactional
    public RegisterBankResponse saveBank(RegisterBankRequest request) {
        Account account = getUserBasicInfo();
        Bank bank = bankRepository.save(modelMapper.map(request, Bank.class));

        bankService.depositTo(bank.getBankName(), bank.getBankAccountNumber(), 1, bank.generateAuthMsg());
        account.addBank(bank);

        return getRegisterBankResponse(bank);
    }

    @Transactional
    public void confirmBank(ConfirmBankRequest request) {
        Account account = getUserBasicInfo();
        Bank bank = bankRepository.findBankByIdAndAccount(request.getBankId(), account);

        if (bank == null) {
            throw new CBankNotFoundException();
        }

        if (!bank.getBankAccountAuthMsg().equals(request.getAuthMsg())) {
            throw new CBankAuthFailedException();
        }

        bank.successBankAccountAuthentication();
        bank.setPasswords(passwordEncoder.encode(request.getBankAccountPassword()), passwordEncoder.encode(request.getPaymentPassword()));
    }

    private RegisterBankResponse getRegisterBankResponse(Bank bank) {
        RegisterBankResponse registerBankResponse = new RegisterBankResponse();
        registerBankResponse.setBankId(bank.getId());
        registerBankResponse.setBankName(bank.getBankName());
        registerBankResponse.setBankAccountNumber(bank.getBankAccountNumber());
        registerBankResponse.setMsg(bank.getBankAccountAuthMsg());      // only console

        return registerBankResponse;
    }

    private Account getUserBasicInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findAccountByEmail(authentication.getName()).orElseThrow(CUserNotFoundException::new);
    }
}
