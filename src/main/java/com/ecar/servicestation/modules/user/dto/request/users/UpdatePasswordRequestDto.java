package com.ecar.servicestation.modules.user.dto.request.users;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class UpdatePasswordRequestDto {

    @ApiParam(value = "현재 비밀번호", required = true)
    @NotBlank
    @Length(max = 100)
    private String currentPassword;

    @ApiParam(value = "새로운 비밀번호", required = true)
    @NotBlank
    @Length(max = 100)
    private String newPassword;

}
