package com.ecar.servicestation.modules.user.dto.request;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class CashOut {

    @ApiParam(value = "금액", required = true)
    @NotNull
    @Min(value = 1)
    private Long amount;
}
