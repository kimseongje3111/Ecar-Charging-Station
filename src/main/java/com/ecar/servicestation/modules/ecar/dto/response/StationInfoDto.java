package com.ecar.servicestation.modules.ecar.dto.response;

import com.ecar.servicestation.modules.ecar.domain.Charger;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class StationInfoDto {

    private Long stationId;

    private Long stationNumber;

    private String stationName;

    private String stationAddress;

    private Double latitude;

    private Double longitude;

    private Set<Charger> chargers = new HashSet<>();

    private boolean bookmarked;
}
