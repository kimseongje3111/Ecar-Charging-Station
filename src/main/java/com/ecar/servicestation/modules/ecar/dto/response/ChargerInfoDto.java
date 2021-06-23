package com.ecar.servicestation.modules.ecar.dto.response;

import com.ecar.servicestation.modules.ecar.domain.Station;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChargerInfoDto {

    private Long chargerId;

    private Long chargerNumber;

    private String chargerName;

    private Integer type;

    private Integer mode;

    private Integer state;

    private LocalDateTime stateUpdatedAt;

    private Station station;
}
