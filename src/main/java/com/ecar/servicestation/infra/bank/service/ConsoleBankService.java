package com.ecar.servicestation.infra.bank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConsoleBankService implements BankService {

    private static final String SERVER_BANK_ACCOUNT = "99999999999";
    private static final String SERVER_BANK_ACCESS_TOKEN = "SERVER_VALID_ACCESS_TOKEN";

    @Override
    public String bankAccountUserAuthentication(String bankName, String accountNumber, long certificateId, String certificatePassword) {
        // 계좌 사용자 인증을 위한 공인인증서 로그인 //
        // 인증서 정보가 유효하다면 해당 계좌의 ACCESS_TOKEN 발급 //

        boolean isValid = true;
        String bankAccountAccessToken = null;

        // 유효하지 않은 인증서일 경우, 예외 처리 //

        if (isValid) {
            bankAccountAccessToken = "VALID_ACCESS_TOKEN";

            log.info("Authentication of the bank account completed.");
            log.info("ACCESS-TOKEN:{}", bankAccountAccessToken);
        }

        return bankAccountAccessToken;
    }

    @Override
    public void depositTo(String bankName, String accountNumber, int amount, String msg) {
        // 서버 계좌로부터 대상 계좌에 금액 송금 //

        log.info("Deposit completed.");
        log.info("FROM:{}, TO:{} ,AMOUNT:{}, MESSAGE:{}", SERVER_BANK_ACCOUNT, accountNumber, amount, msg);
    }

    @Override
    public void withdrawFrom(String bankName, String accountNumber, String accessToken, int amount) {
        // 대상 계좌로부터 서버 계좌로 금액 출금 //
        // 대상 계좌의 ACCESS_TOKEN 유효성 검증 //

        log.info("Withdraw completed.");
        log.info("FROM:{}, TO:{} ,AMOUNT:{}", accountNumber, SERVER_BANK_ACCOUNT, amount);
    }

}
