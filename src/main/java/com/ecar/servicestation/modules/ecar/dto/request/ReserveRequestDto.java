package com.ecar.servicestation.modules.ecar.dto.request;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ReserveRequestDto {

    @ApiParam(value = "충전기 ID", required = true)
    @NotNull
    private Long chargerId;

    @ApiParam(value = "충전 시작 날짜", required = true)
    @NotNull
    private LocalDateTime start;

    @ApiParam(value = "충전 종료 날짜", required = true)
    @NotNull
    private LocalDateTime end;

    @ApiParam(value = "사용자 차량 ID", required = true)
    @NotNull
    private Long carId;

}
