package com.ecar.servicestation.modules.user.dto.request.users;

import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
public class UpdateNotificationRequest {

    @ApiParam(value = "예약 충전 시작 알림 여부", required = true)
    private boolean onNotificationOfReservationStart;

    @ApiParam(value = "예약 충전 시작 알림 시간(기준 @분전)", required = true)
    private Integer minutesBeforeReservationStart;

    @ApiParam(value = "예약 충전 종료 알림 여부", required = true)
    private boolean onNotificationOfChargingEnd;

    @ApiParam(value = "예약 충전 종료 알림 시간(기준 @분전)", required = true)
    private Integer minutesBeforeChargingEnd;

}
