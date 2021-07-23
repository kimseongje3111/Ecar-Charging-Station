package com.ecar.servicestation.modules.user.dto.request.banks;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class RegisterBankRequestDto {

    @ApiParam(name = "은행명", required = true)
    @NotBlank
    private String bankName;

    @ApiParam(name = "계좌번호", required = true)
    @NotBlank
    // @Pattern(regexp = "^[0-9]*$")
    private String bankAccountNumber;

    @ApiParam(name = "계좌 명의", required = true)
    @NotBlank
    private String bankAccountOwner;

    @ApiParam(name = "공인인증서 ID", required = true)
    private Long certificateId;

    @ApiParam(name = "공인인증서 비밀번호", required = true)
    private String certificatePassword;

}
