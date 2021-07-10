package com.ecar.servicestation.modules.ecar.dto.request;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class SearchConditionDto {

    @ApiParam(value = "검색 대상", required = true)
    @NotBlank
    private String search;

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

    @ApiParam(value = "정렬 기준(이름순[0], 거리순[1])", defaultValue = "0")
    @Pattern(regexp = "^[01]$")
    private String sortType = "0";
}
