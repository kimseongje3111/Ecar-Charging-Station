package com.ecar.servicestation.modules.user.dto;

import com.ecar.servicestation.modules.ecar.domain.Station;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserHistory {

    private Station station;

    private Integer chargerCount;

    private LocalDateTime searchedAt;
}