package com.ecar.servicestation.modules.ecar.dto;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class SearchCondition {

    @ApiParam(value = "검색 유형", defaultValue = "0")
    private Integer searchType = 0;

    @ApiParam(value = "검색 대상", required = true)
    private String search;

    @ApiParam(value = "충전기 상태")
    private Integer cpStat;

    @ApiParam(value = "충전기 타입")
    private Integer chargerTp;

    @ApiParam(value = "충전 방식")
    private Integer cpTp;
}
