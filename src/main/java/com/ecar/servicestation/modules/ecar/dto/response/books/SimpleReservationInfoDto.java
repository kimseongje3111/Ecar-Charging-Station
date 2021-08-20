package com.ecar.servicestation.modules.ecar.dto.response.books;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SimpleReservationInfoDto {

    private String stationName;

    private String chargerName;

    private String userName;

    private String carNumber;

    private LocalDateTime chargeStartDateTime;

    private LocalDateTime chargeEndDateTime;

    private Integer fares;

}
