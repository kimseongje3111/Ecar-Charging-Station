package com.ecar.servicestation.infra.address;

import com.ecar.servicestation.infra.address.dto.Address;

import java.util.Set;

public interface AddressService {

    Set<Address> convertRoadAddress(String search, int page, int numberOfRows);
}
