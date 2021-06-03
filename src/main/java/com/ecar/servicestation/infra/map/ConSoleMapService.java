package com.ecar.servicestation.infra.map;

import com.ecar.servicestation.infra.map.dto.MapLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile("local")
public class ConSoleMapService implements MapService {

    @Override
    public MapLocation geoCoding(String address) {
        log.info("This address has been converted to location.");

        return new MapLocation();
    }

    @Override
    public String reverseGeoCoding(MapLocation location) {
        log.info("This latitude/longitude has been converted to an address.");

        return "SUCCESS";
    }
}
