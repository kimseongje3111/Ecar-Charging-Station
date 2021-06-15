package com.ecar.servicestation.modules.user.dto.request;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class ConfirmBankRequest {

    @ApiParam(name = "계좌 ID", required = true)
    @NotNull
    private Long bankId;

    @ApiParam(name = "계좌 비밀번호(4자리)", required = true)
    @NotBlank
    @Pattern(regexp = "^[0-9]{4}$")
    private String bankAccountPassword;

    @ApiParam(name = "결제 비밀번호", required = true)
    @NotBlank
    private String paymentPassword;

    @ApiParam(name = "계좌 인증 메시지", required = true)
    @NotBlank
    private String authMsg;
}
