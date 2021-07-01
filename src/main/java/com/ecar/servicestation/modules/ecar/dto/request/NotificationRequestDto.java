package com.ecar.servicestation.modules.ecar.dto.request;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class NotificationRequestDto {

    @ApiParam(value = "예약명", required = true)
    @NotBlank
    private String reserveTitle;

    @ApiParam(value = "설정 여부", required = true)
    @NotNull
    private Boolean isOn;

    @ApiParam(value = "충전 종료 알림 시간(ex.종료 @분전, 분단위)", defaultValue = "30")
    private Integer minutes = 30;

}
