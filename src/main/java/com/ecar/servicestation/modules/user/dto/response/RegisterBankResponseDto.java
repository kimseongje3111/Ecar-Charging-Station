package com.ecar.servicestation.modules.user.dto.response;

import lombok.Data;

@Data
public class RegisterBankResponseDto {

    private Long bankId;

    private String bankName;

    private String bankAccountNumber;

    // only console
    private String msg;

}
