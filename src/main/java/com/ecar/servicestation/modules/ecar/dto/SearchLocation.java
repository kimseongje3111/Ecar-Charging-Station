package com.ecar.servicestation.modules.ecar.dto;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class SearchLocation {

    @ApiParam(value = "위도", required = true)
    private Long lat;

    @ApiParam(value = "경도", required = true)
    private Long lang;
}
