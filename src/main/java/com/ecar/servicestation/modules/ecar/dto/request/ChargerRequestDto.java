package com.ecar.servicestation.modules.ecar.dto.request;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ChargerRequestDto {

    @ApiParam(value = "충전기 번호", required = true)
    @NotNull
    private Long chargerNumber;

    @ApiParam(value = "예약명", required = true)
    @NotBlank
    private String reserveTitle;

}
