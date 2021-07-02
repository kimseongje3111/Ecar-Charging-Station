package com.ecar.servicestation.modules.user.dto.request;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class EmailAuthRequestDto {

    @ApiParam(value = "회원 이메일", required = true)
    @NotBlank
    @Email
    String email;

    @ApiParam(value = "이메일 계정 인증 토큰", required = true)
    @NotBlank
    String token;
}
