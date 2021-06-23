package com.ecar.servicestation.modules.user.service;

import com.ecar.servicestation.infra.bank.service.BankService;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Bank;
import com.ecar.servicestation.modules.user.dto.request.CashInDto;
import com.ecar.servicestation.modules.user.dto.request.CashOutDto;
import com.ecar.servicestation.modules.user.dto.request.ConfirmBankRequestDto;
import com.ecar.servicestation.modules.user.dto.request.RegisterBankRequestDto;
import com.ecar.servicestation.modules.user.dto.response.RegisterBankResponseDto;
import com.ecar.servicestation.modules.user.dto.response.UserBankDto;
import com.ecar.servicestation.modules.user.exception.CBankAuthFailedException;
import com.ecar.servicestation.modules.user.exception.CBankNotFoundException;
import com.ecar.servicestation.modules.user.exception.CUserCashFailedException;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBankService {

    private final UserRepository userRepository;
    private final BankRepository bankRepository;
    private final BankService bankService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterBankResponseDto saveBank(RegisterBankRequestDto request) {
        Account account = getUserBasicInfo();
        Bank bank = bankRepository.save(modelMapper.map(request, Bank.class));

        bankService.depositTo(bank.getBankName(), bank.getBankAccountNumber(), 1, bank.generateAuthMsg());
        account.addBank(bank);

        return getRegisterBankResponse(bank);
    }

    @Transactional
    public void validateAuthAndConfirmBank(ConfirmBankRequestDto request) {
        Account account = getUserBasicInfo();
        Bank bank = bankRepository.findBankByIdAndAccount(request.getBankId(), account);

        if (bank == null) {
            throw new CBankNotFoundException();
        }

        if (!bank.getBankAccountAuthMsg().equals(request.getAuthMsg())) {
            throw new CBankAuthFailedException();
        }

        String accessToken =
                bankService.bankAccountUserAuthentication(
                        bank.getBankName(),
                        bank.getBankAccountNumber(),
                        request.getCertificateId(),
                        request.getCertificatePassword()
                );

        bank.successBankAccountAuthentication();
        bank.setPaymentPasswordAndAccessToken(passwordEncoder.encode(request.getPaymentPassword()), accessToken);
    }

    public List<UserBankDto> getMyBanks() {
        return getUserBasicInfo().getMyBanks()
                .stream()
                .filter(Bank::isBankAccountVerified)
                .map(myBank -> {
                    UserBankDto userBank = modelMapper.map(myBank, UserBankDto.class);
                    userBank.setBankId(myBank.getId());

                    return userBank;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteBank(Long id) {
        Account account = getUserBasicInfo();
        Bank bank = bankRepository.findBankByIdAndAccount(id, account);

        if (bank == null || !bank.isBankAccountVerified()) {
            throw new CBankNotFoundException();
        }

        bankRepository.delete(account.removeBank(bank));
    }

    @Transactional
    public void changeMainUsedBank(Long id) {
        Account account = getUserBasicInfo();
        Bank bank = bankRepository.findBankByIdAndAccount(id, account);

        if (bank == null || !bank.isBankAccountVerified()) {
            throw new CBankNotFoundException();

        } else {
            account.getMyBanks().forEach(myBank -> myBank.setMainUsed(myBank.equals(bank)));
        }
    }

    @Transactional
    public void chargeCash(CashInDto cashIn) {
        Account account = getUserBasicInfo();
        Bank mainUsedBank = account.getMyMainUsedBank();

        if (mainUsedBank == null) {
            throw new CBankNotFoundException();
        }

        if (!passwordEncoder.matches(cashIn.getPaymentPassword(), mainUsedBank.getPaymentPassword())) {
            throw new CUserCashFailedException();
        }

        bankService.withdrawFrom(mainUsedBank.getBankName(), mainUsedBank.getBankAccountNumber(), mainUsedBank.getBankAccountAccessToken(), cashIn.getAmount());
        account.chargingCash(cashIn.getAmount().intValue());
    }

    @Transactional
    public void refundCash(CashOutDto cashOut) {
        Account account = getUserBasicInfo();
        Bank mainUsedBank = account.getMyMainUsedBank();

        if (mainUsedBank == null) {
            throw new CBankNotFoundException();
        }

        if (cashOut.getAmount() > account.getCash()) {
            throw new CUserCashFailedException();
        }

        bankService.depositTo(mainUsedBank.getBankName(), mainUsedBank.getBankAccountNumber(), cashOut.getAmount(), "");
        account.paymentOrRefundCash(cashOut.getAmount().intValue());
    }

    private RegisterBankResponseDto getRegisterBankResponse(Bank bank) {
        RegisterBankResponseDto response = new RegisterBankResponseDto();
        response.setBankId(bank.getId());
        response.setBankName(bank.getBankName());
        response.setBankAccountNumber(bank.getBankAccountNumber());
        response.setMsg(bank.getBankAccountAuthMsg());      // only console

        return response;
    }

    private Account getUserBasicInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findAccountByEmail(authentication.getName()).orElseThrow(CUserNotFoundException::new);
    }
}
