package com.ecar.servicestation.modules.ecar.dto.response.books;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationStatementDto {

    private Long reservationId;

    private Long chargerId;

    private String reserveTitle;

    private String userName;

    private String carNumber;

    private String state;

    private LocalDateTime reservedAt;

    private LocalDateTime chargeStartDateTime;

    private LocalDateTime chargeEndDateTime;

    private Integer reserveFares;

    private Integer usedCashPoint;

    private Integer paidCash;

    private Integer cancellationFee;

}
