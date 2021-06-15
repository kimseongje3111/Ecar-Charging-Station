package com.ecar.servicestation.modules.ecar.dto.request;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
public class ReserveRequest {

    @ApiParam(value = "충전기 ID", required = true)
    @NonNull
    private Long chargerId;

    @ApiParam(value = "충전 시작 날짜", required = true)
    @NonNull
    private LocalDateTime start;

    @ApiParam(value = "충전 종료 날짜", required = true)
    @NonNull
    private LocalDateTime end;
}
