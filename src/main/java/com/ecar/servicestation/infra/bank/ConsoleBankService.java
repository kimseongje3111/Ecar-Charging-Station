package com.ecar.servicestation.infra.bank;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConsoleBankService implements BankService {

    @Override
    public void depositTo(String bankName, String accountNumber, long amount, String msg) {
        // TODO : 존재하지 않거나 만료된 계좌인 경우 Exception 처리

        log.info("Deposit completed.");
    }
}
