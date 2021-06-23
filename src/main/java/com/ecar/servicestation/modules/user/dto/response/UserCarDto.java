package com.ecar.servicestation.modules.user.dto.response;

import lombok.Data;

@Data
public class UserCarDto {

    private Long carId;

    private String carModel;

    private String carModelYear;

    private String carType;

    private String carNumber;
}
