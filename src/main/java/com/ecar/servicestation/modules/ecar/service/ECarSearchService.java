package com.ecar.servicestation.modules.ecar.service;

import com.ecar.servicestation.infra.data.ECarChargingStationInfoProvider;
import com.ecar.servicestation.infra.data.dto.EVInfo;
import com.ecar.servicestation.infra.map.MapService;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.ecar.dto.SearchCondition;
import com.ecar.servicestation.modules.ecar.dto.SearchLocation;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ECarSearchService {

    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final ECarChargingStationInfoProvider eCarChargingStationInfoProvider;
    private final MapService mapService;

    @Transactional
    public List<Charger> getSearchResults(SearchCondition condition) {
        List<EVInfo> evInfos =
                eCarChargingStationInfoProvider.getData(
                        condition.getAddress(),
                        condition.getPageable().getPageNumber() + 1,
                        condition.getPageable().getPageSize()
                );

        return chargerRepository.findAllWithStationBySearchCondition(getUpdatedChargers(evInfos), condition);
    }

    @Transactional
    public List<Charger> getSearchResultsByLocation(SearchLocation location) {
        String address = mapService.reverseGeoCoding(location.getLat(), location.getLang());
        List<EVInfo> evInfos = eCarChargingStationInfoProvider.getData(address, 1, 10);

        return chargerRepository.findAllWithStation(getUpdatedChargers(evInfos));
    }

    private List<Long> getUpdatedChargers(List<EVInfo> evInfos) {
        List<Long> chargerIds = new ArrayList<>();

        for (EVInfo evInfo : evInfos) {
            Station station = stationRepository.findStationByStationNumber(evInfo.getStationNumber());
            Charger charger = chargerRepository.findChargerByChargerNumber(evInfo.getChargerNumber());

            if (station == null) {
                station = stationRepository.save(evInfo.EVInfoToStation());
            }

            if (charger == null) {
                charger = chargerRepository.save(evInfo.EVInfoToCharger());
            }

            if (charger.isRequiredUpdate(evInfo.getChargerStateUpdatedAt())) {
                charger.setState(evInfo.getChargerState());
                charger.setStateUpdatedAt(evInfo.getChargerStateUpdatedAt());
            }

            station.addCharger(charger);
            chargerIds.add(charger.getId());
        }

        return chargerIds;
    }
}
