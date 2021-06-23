package com.ecar.servicestation.modules.ecar.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReserveResponseDto {

    private Long reservationId;

    private String userName;

    private String carNumber;

    private Long chargerId;

    private LocalDateTime reservedAt;

    private String state;

    private Integer fares;
}
