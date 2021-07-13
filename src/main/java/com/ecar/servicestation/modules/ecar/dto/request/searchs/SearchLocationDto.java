package com.ecar.servicestation.modules.ecar.dto.request.searchs;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SearchLocationDto {

    @ApiParam(value = "충전 방식")
    private Integer cpTp;

    @ApiParam(value = "충전기 타입")
    private Integer chargerTp;

    @ApiParam(value = "위도", required = true)
    @NotNull
    private Double latitude;

    @ApiParam(value = "경도", required = true)
    @NotNull
    private Double longitude;

}
