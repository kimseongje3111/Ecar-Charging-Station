package com.ecar.servicestation.modules.ecar.factory;

import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ECarFactory {

    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;

    @Transactional
    public Station createStationAndAddCharger() {
        Station station =
                stationRepository.save(
                        Station.builder()
                                .stationNumber(1L)
                                .stationAddress("TEST ADDRESS")
                                .stationName("TEST STATION")
                                .latitude(Double.valueOf("36.357692"))
                                .longitude(Double.valueOf("127.381050"))
                                .build()
                );

        Charger charger = chargerRepository.save(
                Charger.builder()
                        .chargerNumber(1L)
                        .chargerName("TEST CHARGER")
                        .type(2)
                        .mode(1)
                        .state(1)
                        .stateUpdatedAt(LocalDateTime.now())
                        .build()
        );

        station.addCharger(charger);

        return station;
    }
}
