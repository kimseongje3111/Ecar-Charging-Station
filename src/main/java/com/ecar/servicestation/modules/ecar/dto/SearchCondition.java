package com.ecar.servicestation.modules.ecar.dto;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.springframework.data.domain.Pageable;

@Data
public class SearchCondition {

    @ApiParam(value = "검색 대상 주소", required = true)
    private String address;

    @ApiParam(value = "충전기 상태")
    private Integer cpStat;

    @ApiParam(value = "충전기 타입")
    private Integer chargerTp;

    @ApiParam(value = "충전 방식")
    private Integer cpTp;

    @ApiParam(value = "페이지")
    private Pageable pageable;
}
