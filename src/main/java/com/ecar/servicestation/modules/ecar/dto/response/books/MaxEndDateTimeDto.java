package com.ecar.servicestation.modules.ecar.dto.response.books;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MaxEndDateTimeDto {

    private Long chargerId;

    private LocalDateTime targetDateTime;

    private LocalDateTime maxEndDateTime;

    private Integer faresPerHour;
}
