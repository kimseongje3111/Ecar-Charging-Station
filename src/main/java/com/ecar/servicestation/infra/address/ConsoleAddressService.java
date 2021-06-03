package com.ecar.servicestation.infra.address;

import com.ecar.servicestation.infra.address.dto.Address;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
@Profile("local")
public class ConsoleAddressService implements AddressService {

    @Override
    public Set<Address> convertRoadAddress(String search, int page, int numberOfRows) {
        return null;
    }
}
