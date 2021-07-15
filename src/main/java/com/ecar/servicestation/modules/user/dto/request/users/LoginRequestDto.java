package com.ecar.servicestation.modules.user.dto.request.users;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class LoginRequestDto {

    @ApiParam(value = "사용자 이메일(아이디)", required = true)
    @NotBlank
    @Email
    @Length(max = 30)
    private String email;

    @ApiParam(value = "비밀번호", required = true)
    @NotBlank
    @Length(max = 100)
    private String password;

    @ApiParam(value = "디바이스 토큰", required = true)
    @NotBlank
    private String deviceToken;

}
