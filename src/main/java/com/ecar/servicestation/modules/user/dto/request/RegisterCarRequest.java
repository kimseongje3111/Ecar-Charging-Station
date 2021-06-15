package com.ecar.servicestation.modules.user.dto.request;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RegisterCarRequest {

    @ApiParam(name = "차량 모델", required = true)
    @NotBlank
    private String carModel;

    @ApiParam(name = "차량 모델 연도", required = true)
    @NotBlank
    private String carModelYear;

    @ApiParam(name = "차량 유형", required = true)
    @NotBlank
    private String carType;

    @ApiParam(name = "차량 번호", required = true)
    @NotBlank
    private String carNumber;

}
