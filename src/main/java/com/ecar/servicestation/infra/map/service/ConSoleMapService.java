package com.ecar.servicestation.infra.map.service;

import com.ecar.servicestation.infra.map.dto.MapLocationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile("local")
public class ConSoleMapService implements MapService {

    @Override
    public MapLocationDto geoCoding(String address) {
        log.info("This address has been converted to location.");

        return new MapLocationDto();
    }

    @Override
    public String reverseGeoCoding(MapLocationDto location) {
        log.info("This latitude/longitude has been converted to an address.");

        return "SUCCESS";
    }
}
