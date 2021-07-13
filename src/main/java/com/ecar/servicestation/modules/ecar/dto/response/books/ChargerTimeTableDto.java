package com.ecar.servicestation.modules.ecar.dto.response.books;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class ChargerTimeTableDto {

    private Long chargerId;

    private LocalDate targetDate;

    private Map<LocalDateTime, Boolean> timeTable = new HashMap<>();
}
