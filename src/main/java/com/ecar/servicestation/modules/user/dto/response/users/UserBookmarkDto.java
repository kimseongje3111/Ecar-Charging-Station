package com.ecar.servicestation.modules.user.dto.response.users;

import com.ecar.servicestation.modules.ecar.domain.Station;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserBookmarkDto {

    private Station station;

    private Integer chargerCount;

    private LocalDateTime registeredAt;

}
