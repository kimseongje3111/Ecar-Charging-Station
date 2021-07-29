package com.ecar.servicestation.modules.ecar.dto.response.books;

import com.ecar.servicestation.modules.ecar.domain.Charger;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReserveResponseDto {

    private Long reservationId;

    private String userName;

    private String carNumber;

    private Charger charger;

    private LocalDateTime reservedAt;

    private String state;

    private Integer fares;
}
