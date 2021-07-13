package com.ecar.servicestation.modules.user.service;

import com.ecar.servicestation.infra.bank.service.BankService;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Bank;
import com.ecar.servicestation.modules.user.dto.request.banks.CashInRequestDto;
import com.ecar.servicestation.modules.user.dto.request.banks.CashOutRequestDto;
import com.ecar.servicestation.modules.user.dto.request.banks.AuthBankRequestDto;
import com.ecar.servicestation.modules.user.dto.request.banks.RegisterBankRequestDto;
import com.ecar.servicestation.modules.user.dto.response.RegisterBankResponseDto;
import com.ecar.servicestation.modules.user.dto.response.users.UserBankDto;
import com.ecar.servicestation.modules.user.exception.banks.CBankAuthFailedException;
import com.ecar.servicestation.modules.user.exception.banks.CBankNotFoundException;
import com.ecar.servicestation.modules.user.exception.banks.CUserCashFailedException;
import com.ecar.servicestation.modules.user.exception.users.CUserNotFoundException;
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
    public RegisterBankResponseDto registerUserBankAccount(RegisterBankRequestDto request) {
        Account account = getLoginUserContext();
        Bank bank = bankRepository.save(modelMapper.map(request, Bank.class));

        bankService.depositTo(bank.getBankName(), bank.getBankAccountNumber(), 1, bank.generateAuthMsg());
        account.addBank(bank);

        return getRegisterBankResponse(bank);
    }

    @Transactional
    public void deleteUserBankAccount(long bankId) {
        Account account = getLoginUserContext();
        Bank bank = bankRepository.findBankByIdAndAccount(bankId, account);

        if (bank == null || !bank.isBankAccountVerified()) {
            throw new CBankNotFoundException();
        }

        bankRepository.delete(account.removeBank(bank));
    }

    public List<UserBankDto> getUserBankAccounts() {
        Account account = getLoginUserContext();
        List<Bank> myBanks = account.getMyBanks();

        return myBanks.stream()
                .filter(Bank::isBankAccountVerified)
                .map(myBank -> {
                    UserBankDto userBank = modelMapper.map(myBank, UserBankDto.class);
                    userBank.setBankId(myBank.getId());

                    return userBank;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void validateAuthAndConfirmUserBankAccount(AuthBankRequestDto request) {
        Account account = getLoginUserContext();
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

    @Transactional
    public void changeMainUsedBank(long bankId) {
        Account account = getLoginUserContext();
        Bank bank = bankRepository.findBankByIdAndAccount(bankId, account);

        if (bank == null || !bank.isBankAccountVerified()) {
            throw new CBankNotFoundException();

        } else {
            account.getMyBanks().forEach(myBank -> myBank.setMainUsed(myBank.equals(bank)));
        }
    }

    @Transactional
    public void chargeCashFromMainUsedBankAccount(CashInRequestDto request) {
        Account account = getLoginUserContext();
        Bank mainUsedBank = account.getMyMainUsedBank();

        if (mainUsedBank == null) {
            throw new CBankNotFoundException();
        }

        if (!passwordEncoder.matches(request.getPaymentPassword(), mainUsedBank.getPaymentPassword())) {
            throw new CUserCashFailedException();
        }

        bankService.withdrawFrom(mainUsedBank.getBankName(), mainUsedBank.getBankAccountNumber(), mainUsedBank.getBankAccountAccessToken(), request.getAmount());
        account.chargingCash(request.getAmount());
    }

    @Transactional
    public void refundCashToMainUsedBankAccount(CashOutRequestDto request) {
        Account account = getLoginUserContext();
        Bank mainUsedBank = account.getMyMainUsedBank();

        if (mainUsedBank == null) {
            throw new CBankNotFoundException();
        }

        if (request.getAmount() > account.getCash()) {
            throw new CUserCashFailedException();
        }

        bankService.depositTo(mainUsedBank.getBankName(), mainUsedBank.getBankAccountNumber(), request.getAmount(), "");
        account.paymentOrRefundCash(request.getAmount());
    }

    private Account getLoginUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findAccountByEmail(authentication.getName()).orElseThrow(CUserNotFoundException::new);
    }

    private RegisterBankResponseDto getRegisterBankResponse(Bank bank) {
        RegisterBankResponseDto response = new RegisterBankResponseDto();
        response.setBankId(bank.getId());
        response.setBankName(bank.getBankName());
        response.setBankAccountNumber(bank.getBankAccountNumber());
        response.setMsg(bank.getBankAccountAuthMsg());      // only console

        return response;
    }

}
