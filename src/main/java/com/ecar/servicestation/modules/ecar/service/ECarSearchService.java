package com.ecar.servicestation.modules.ecar.service;

import com.ecar.servicestation.infra.address.service.AddressService;
import com.ecar.servicestation.infra.address.dto.AddressDto;
import com.ecar.servicestation.infra.data.dto.EVInfoDto;
import com.ecar.servicestation.infra.data.exception.EVINfoNotFoundException;
import com.ecar.servicestation.infra.map.dto.MapLocationDto;
import com.ecar.servicestation.infra.map.service.MapService;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.ecar.dto.request.searchs.SearchConditionDto;
import com.ecar.servicestation.modules.ecar.dto.request.searchs.SearchLocationDto;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.StationRepository;
import com.ecar.servicestation.modules.ecar.service.SearchMapConverter.Node;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ECarSearchService {

    private static final String SORT_TYPE_NAME = "0";
    private static final String SORT_TYPE_DISTANCE = "1";

    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final ECarSearchAsyncService eCarSearchAsyncService;
    private final AddressService addressService;
    private final MapService mapService;
    private final ModelMapper modelMapper;

    @Transactional
    public List<Charger> getSearchResultsBy(SearchConditionDto condition, Pageable pageable) {
        List<CompletableFuture<Set<EVInfoDto>>> futures = new ArrayList<>();
        Set<EVInfoDto> evInfoSet = new HashSet<>();

        // 충전소 API 호출 //

        searchAsAsyncByLongestPrefixes(futures, preProcessingAndExtractSearchKeywords(condition.getSearch()), 0);

        // 결과 병합 //

        CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenApply(result -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()))
                .join()
                .forEach(evInfoSet::addAll);

        return getUpdatedChargers(evInfoSet, condition, null);
    }

    @Transactional
    public List<Charger> getSearchResultsBy(SearchLocationDto location, Pageable pageable) {
        String search = mapService.reverseGeoCoding(modelMapper.map(location, MapLocationDto.class));
        Set<EVInfoDto> evInfoSet = new HashSet<>(eCarSearchAsyncService.getSearchResult(search, 0).join());

        return getUpdatedChargers(evInfoSet, null, location);
    }

    private Node preProcessingAndExtractSearchKeywords(String search) {
        SearchMapConverter searchMapConverter = new SearchMapConverter();
        Set<AddressDto> address = addressService.convertToAddressDto(search, 1, 100);

        address.stream()
                .collect(Collectors.groupingBy(AddressDto::getOldAddress))
                .forEach((oldAddress, values) -> {
                    searchMapConverter.insert(oldAddress);
                    values.stream().map(AddressDto::getNewAddress).forEach(searchMapConverter::insert);
                });

        return searchMapConverter.getRoot();
    }

    private void searchAsAsyncByLongestPrefixes(List<CompletableFuture<Set<EVInfoDto>>> futures, Node node, int depth) {
        if (depth == 3) {
            futures.add(eCarSearchAsyncService.getSearchResult(node.getTResult(), 0));

        } else {
            for (String key : node.getNodeMap().keySet()) {
                searchAsAsyncByLongestPrefixes(futures, node.getNodeMap().get(key), depth + 1);
            }
        }
    }

    private List<Charger> getUpdatedChargers(Set<EVInfoDto> evInfoSet, SearchConditionDto condition, SearchLocationDto location) {
        if (evInfoSet.size() == 0) {
            throw new EVINfoNotFoundException();
        }

        List<Charger> chargers = new ArrayList<>();

        evInfoSet.forEach(evInfo -> {
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
            chargers.add(charger);
        });

        String sortType;
        double myLat, myLongi;
        List<Charger> fChargers = chargers;

        if (condition != null) {
            sortType = condition.getSortType();
            myLat = condition.getLatitude();
            myLongi = condition.getLongitude();

            if (condition.getCpTp() != null) {
                fChargers = fChargers.stream().filter(charger -> charger.getType().equals(condition.getCpTp())).collect(Collectors.toList());
            }

            if (condition.getChargerTp() != null) {
                fChargers = fChargers.stream().filter(charger -> charger.getMode().equals(condition.getChargerTp())).collect(Collectors.toList());
            }

        } else {
            sortType = SORT_TYPE_DISTANCE;
            myLat = location.getLatitude();
            myLongi = location.getLongitude();

            if (location.getCpTp() != null) {
                fChargers = fChargers.stream().filter(charger -> charger.getType().equals(location.getCpTp())).collect(Collectors.toList());
            }

            if (location.getChargerTp() != null) {
                fChargers = fChargers.stream().filter(charger -> charger.getMode().equals(location.getChargerTp())).collect(Collectors.toList());
            }
        }

        if (sortType.equals(SORT_TYPE_NAME)) {
            return fChargers.stream()
                    .sorted(Comparator.comparing(charger -> charger.getStation().getStationAddress()))
                    .collect(Collectors.toList());

        } else {
            return fChargers.stream()
                    .sorted(
                            Comparator.comparingDouble(charger -> {
                                Station station = charger.getStation();
                                return calDistance(myLat, myLongi, station.getLatitude(), station.getLongitude());
                            })
                    )
                    .collect(Collectors.toList());
        }
    }

    /**
     * @source 과일가게 개발자
     * @link https://fruitdev.tistory.com/189
     */
    private double calDistance(double srcLat, double srcLongi, double dstLat, double dstLongi) {
        double theta = srcLongi - dstLongi;
        double distance = Math.sin(degreeToRadian(srcLat)) * Math.sin(degreeToRadian(dstLat))
                + Math.cos(degreeToRadian(srcLat)) * Math.cos(degreeToRadian(dstLat)) * Math.cos(degreeToRadian(theta));

        distance = Math.acos(distance);
        distance = radianToDegree(distance);
        distance = distance * 60 * 1.1515 * 1.609344;

        return Math.abs(distance);
    }

    private double degreeToRadian(double degree) {
        return (degree * Math.PI / 180.0);
    }

    private double radianToDegree(double radian) {
        return (radian * 180 / Math.PI);
    }

}
