package com.ecar.servicestation.modules.ecar.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationStatementDto {

    private String reserveTitle;

    private String userName;

    private String carNumber;

    private LocalDateTime reservedAt;

    private LocalDateTime chargeStartDateTime;

    private LocalDateTime chargeEndDateTime;

    private String state;

    private Integer usedCashPoint;

    private Integer paidCash;

    private Integer cancellationFee;
}