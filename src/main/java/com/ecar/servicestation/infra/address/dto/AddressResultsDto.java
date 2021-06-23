package com.ecar.servicestation.infra.address.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddressResultsDto {

    private AddressCommonDto common;

    private List<AddressDto> juso;
}
