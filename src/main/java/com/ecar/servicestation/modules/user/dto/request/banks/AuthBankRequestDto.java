package com.ecar.servicestation.modules.user.dto.request.banks;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AuthBankRequestDto {

    @ApiParam(name = "계좌 ID", required = true)
    @NotNull
    private Long bankId;

    @ApiParam(name = "결제 비밀번호", required = true)
    @NotBlank
    private String paymentPassword;

    @ApiParam(name = "계좌 인증 메시지", required = true)
    @NotBlank
    private String authMsg;

    @ApiParam(name = "공인인증서 ID", required = true)
    private Long certificateId;

    @ApiParam(name = "공인인증서 비밀번호", required = true)
    private String certificatePassword;

}
