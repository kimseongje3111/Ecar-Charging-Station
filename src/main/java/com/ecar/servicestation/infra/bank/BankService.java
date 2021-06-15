package com.ecar.servicestation.infra.bank;

public interface BankService {

    void depositTo(String bankName, String accountNumber, long amount, String msg);
}
