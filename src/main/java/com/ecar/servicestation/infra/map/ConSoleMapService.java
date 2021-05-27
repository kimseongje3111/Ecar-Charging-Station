package com.ecar.servicestation.infra.map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile({"local", "test"})
public class ConSoleMapService implements MapService {

    @Override
    public String reverseGeoCoding(long lat, long lang) {
        log.info("This latitude/longitude has been converted to an address.");

        return "SUCCESS";
    }
}
