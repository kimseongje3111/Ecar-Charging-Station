package com.ecar.servicestation.modules.user.dto.request.users;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateUserRequestDto {

    @ApiParam(value = "사용자 이름")
    @Length(max = 100)
    private String userName;

    @ApiParam(value = "휴대폰 번호")
    private String phoneNumber;

}
