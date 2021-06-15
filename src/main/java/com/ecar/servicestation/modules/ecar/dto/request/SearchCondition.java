package com.ecar.servicestation.modules.ecar.dto.request;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class SearchCondition {

    @ApiParam(value = "검색 유형", defaultValue = "0")
    @Pattern(regexp = "^[01]$")
    private Integer searchType = 0;

    @ApiParam(value = "검색 대상", required = true)
    @NotNull
    private String search;

    @ApiParam(value = "충전기 상태")
    private Integer cpStat;

    @ApiParam(value = "충전기 타입")
    private Integer chargerTp;

    @ApiParam(value = "충전 방식")
    private Integer cpTp;
}
