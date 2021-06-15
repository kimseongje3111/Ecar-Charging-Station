package com.ecar.servicestation.modules.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RegisterCarRequest {

    @NotBlank
    private String carModel;

    @NotBlank
    private String carModelYear;

    @NotBlank
    private String carType;

    @NotBlank
    private String carNumber;

}
