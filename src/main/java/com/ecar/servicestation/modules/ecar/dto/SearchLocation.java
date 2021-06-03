package com.ecar.servicestation.modules.ecar.dto;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class SearchLocation {

    @ApiParam(value = "위도", required = true)
    private Double latitude;

    @ApiParam(value = "경도", required = true)
    private Double longitude;
}
