package com.ecar.servicestation.modules.user.dto.response.users;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserBankDto {

    private Long bankId;

    private String bankName;

    private String bankAccountNumber;

    private String bankAccountOwner;

    private boolean bankAccountVerified;

    private boolean mainUsed;

    private LocalDateTime registeredAt;

}
