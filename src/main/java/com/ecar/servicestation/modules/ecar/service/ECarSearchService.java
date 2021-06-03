package com.ecar.servicestation.modules.ecar.service;

import com.ecar.servicestation.infra.address.AddressService;
import com.ecar.servicestation.infra.address.dto.Address;
import com.ecar.servicestation.infra.data.ECarChargingStationInfoProvider;
import com.ecar.servicestation.infra.data.dto.EVInfo;
import com.ecar.servicestation.infra.data.exception.EVINfoNotFoundException;
import com.ecar.servicestation.infra.map.MapService;
import com.ecar.servicestation.infra.map.dto.MapLocation;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.ecar.dto.SearchCondition;
import com.ecar.servicestation.modules.ecar.dto.SearchLocation;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ECarSearchService {

    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final AddressService addressService;
    private final ECarChargingStationInfoProvider eCarChargingStationInfoProvider;
    private final MapService mapService;
    private final int API_REQUEST_MAX_COUNT = 30;

    private Map<String, String> zoneMap;

    @PostConstruct
    protected void init() throws IOException {
        zoneMap = new HashMap<>();

        Resource resource = new ClassPathResource("zones_kr.csv");
        List<String> lines = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8);

        for (String line : lines) {
            String[] split = line.split(",");
            zoneMap.put(split[0], split[1]);
        }
    }

    @Transactional
    public List<Charger> getSearchResults(SearchCondition condition, Pageable pageable) {
        if (condition.getSearch() == null) {
            throw new EVINfoNotFoundException();
        }

        // 주소(0) 또는 충전소명(1) 기반 검색 //

        Set<EVInfo> evInfos = new HashSet<>();

        if (condition.getSearchType() == 0) {
            List<String> searchList = dataPreprocessing(condition.getSearch());

            if (searchList.size() == 0) {
                throw new EVINfoNotFoundException();
            }

            for (int i = 0; i < searchList.size(); i++) {
                if ((i + 1) % API_REQUEST_MAX_COUNT == 0) {
                    try {
                        Thread.sleep(1000);

                    } catch (InterruptedException e) {
                        log.error("thread error");

                        throw new RuntimeException(e);
                    }
                }

                evInfos.addAll(eCarChargingStationInfoProvider.getData(searchList.get(i), 1, 50));
            }

        } else {
            evInfos =
                    eCarChargingStationInfoProvider.getData(condition.getSearch(), 1, 50)
                            .stream()
                            .filter(evInfo -> evInfo.getStationName().contains(condition.getSearch()))
                            .collect(Collectors.toSet());
        }

        return chargerRepository.findAllWithStationBySearchConditionAndPaging(getUpdatedChargers(evInfos), condition, pageable).getContent();
    }

    @Transactional
    public List<Charger> getSearchResultsByLocation(SearchLocation location, Pageable pageable) {
        if (location.getLatitude() == null || location.getLongitude() == null) {
            throw new EVINfoNotFoundException();
        }

        MapLocation mapLocation = new MapLocation();
        mapLocation.setLatitude(location.getLatitude());
        mapLocation.setLongitude(location.getLongitude());

        Set<EVInfo> evInfos = new HashSet<>();
        List<String> searchList = dataPreprocessing(mapService.reverseGeoCoding(mapLocation));

        if (searchList.size() == 0) {
            throw new EVINfoNotFoundException();
        }

        for (int i = 0; i < searchList.size(); i++) {
            if ((i + 1) % API_REQUEST_MAX_COUNT == 0) {
                try {
                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    log.error("thread error");

                    throw new RuntimeException(e);
                }
            }

            evInfos.addAll(eCarChargingStationInfoProvider.getData(searchList.get(i), 1, 50));
        }

        return chargerRepository.findAllWithStationByPaging(getUpdatedChargers(evInfos), pageable).getContent();
    }

    private List<String> dataPreprocessing(String search) {
        Set<Address> addresses = addressService.convertRoadAddress(search, 1, 100);
        Set<String> searchSet = new HashSet<>();

        addresses.stream()
                .collect(Collectors.groupingBy(Address::getOldAddress))
                .forEach((oldAddress, values) -> {
                    String[] split = oldAddress.split(" ");
                    String siNm = split[0];
                    String sggNm = split[1];
                    String emdNmOrRn = split[2];

                    if (zoneMap.containsKey(siNm)) {
                        searchSet.add(zoneMap.get(siNm) + " " + sggNm + " " + emdNmOrRn.charAt(0));
                    }

                    searchSet.add(siNm + " " + sggNm + " " + emdNmOrRn.charAt(0));
                    searchSet.addAll(values.stream().map(Address::getPrefixOfNewAddress).collect(Collectors.toList()));
                });

        return searchSet.stream().sorted().collect(Collectors.toList());
    }

    private List<Long> getUpdatedChargers(Set<EVInfo> evInfos) {
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
