package com.ecar.servicestation.modules.ecar.dto.response.books;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReserveResponseDto {

    private Long reservationId;

    private Long chargerId;

    private String userName;

    private String carNumber;

    private LocalDateTime reservedAt;

    private String state;

    private Integer fares;
}
