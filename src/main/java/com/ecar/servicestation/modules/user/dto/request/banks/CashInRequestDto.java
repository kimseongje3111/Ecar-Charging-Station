package com.ecar.servicestation.modules.user.dto.request.banks;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CashInRequestDto {

    @ApiParam(value = "금액(최소 1000원)", required = true)
    @NotNull
    @Min(value = 1000)
    private Integer amount;

    @ApiParam(value = "결제 비밀번호", required = true)
    @NotBlank
    private String paymentPassword;

}
