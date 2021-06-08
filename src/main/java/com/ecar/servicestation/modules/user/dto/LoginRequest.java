package com.ecar.servicestation.modules.user.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {

    @NotBlank
    @Email
    @Length(max = 30)
    private String email;

    @NotBlank
    @Length(max = 100)
    private String password;

}
