package com.ecar.servicestation.modules.ecar.dto.request;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SearchLocationDto {

    @ApiParam(value = "위도", required = true)
    @NotNull
    private Double latitude;

    @ApiParam(value = "경도", required = true)
    @NotNull
    private Double longitude;
}
