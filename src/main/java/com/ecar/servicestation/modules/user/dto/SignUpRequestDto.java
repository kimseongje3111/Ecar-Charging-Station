package com.ecar.servicestation.modules.user.dto;

import lombok.Data;

@Data
public class SignUpRequestDto {

    private String userName;

    private String password;

    private String email;

}

