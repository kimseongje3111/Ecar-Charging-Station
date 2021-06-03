package com.ecar.servicestation.infra.address.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddressResults {

    private AddressCommon common;

    private List<Address> juso;
}
