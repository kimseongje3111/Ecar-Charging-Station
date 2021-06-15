package com.ecar.servicestation.modules.ecar.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReserveResponse {

    private Long chargerId;

    private String userName;

    private Long reservationId;

    private LocalDateTime reservedAt;

    private String state;

    private Integer fares;
}
