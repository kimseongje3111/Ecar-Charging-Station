package com.ecar.servicestation.infra.address.service;

import com.ecar.servicestation.infra.address.dto.AddressDto;

import java.util.Set;

public interface AddressService {

    Set<AddressDto> convertRoadAddress(String search, int page, int numberOfRows);
}
