package com.ecar.servicestation.infra.bank.service;

public interface BankService {

    String bankAccountUserAuthentication(String bankName, String accountNumber, long certificateId, String certificatePassword);

    void depositTo(String bankName, String accountNumber, int amount, String msg);

    void withdrawFrom(String bankName, String accountNumber, String accessToken, int amount);

}
