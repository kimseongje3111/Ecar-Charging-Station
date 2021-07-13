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
import java.util.concurrent.ExecutionException;
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
        futures.add(eCarSearchAsyncService.getSearchResult(condition.getSearch(), 0));      // 충전소명 검색 키워드

        Node searchKeywordMap = preProcessingAndExtractSearchKeywords(condition.getSearch());
        searchAsAsyncByLongestPrefixes(futures, searchKeywordMap, 0);

        // 결과 병합 //

        List<Set<EVInfoDto>> join =
                CompletableFuture
                        .allOf(futures.toArray(new CompletableFuture[futures.size()]))
                        .thenApply(result -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()))
                        .join();

        Set<EVInfoDto> evInfoSet = new HashSet<>();
        join.forEach(evInfoSet::addAll);

        return chargerRepository
                .findAllWithStationBySearchConditionAndPaging(
                        getUpdatedChargers(evInfoSet, condition.getSortType(), condition.getLatitude(), condition.getLongitude()),
                        condition,
                        pageable
                )
                .getContent();
    }

    @Transactional
    public List<Charger> getSearchResultsBy(SearchLocationDto location, Pageable pageable) {
        String search = mapService.reverseGeoCoding(modelMapper.map(location, MapLocationDto.class));
        Set<EVInfoDto> evInfoSet = new HashSet<>(eCarSearchAsyncService.getSearchResult(search, 0).join());

        return chargerRepository
                .findAllWithStationBySearchLocationAndPaging(
                        getUpdatedChargers(evInfoSet, SORT_TYPE_DISTANCE, location.getLatitude(), location.getLongitude()),
                        location,
                        pageable
                )
                .getContent();
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
        if (depth >= 3) {
            if (node.isLeafNode() || node.getNodeMap().size() >= 2) {

                // 비동기 이벤트 //

                futures.add(eCarSearchAsyncService.getSearchResult(node.getTResult(), 0));
            }

        } else {
            for (String key : node.getNodeMap().keySet()) {
                searchAsAsyncByLongestPrefixes(futures, node.getNodeMap().get(key), ++depth);
            }
        }
    }

    private List<Long> getUpdatedChargers(Set<EVInfoDto> evInfoSet, String sortType, Double myLat, Double myLongi) {
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

        if (sortType.equals(SORT_TYPE_NAME)) {
            return chargers.stream()
                    .sorted(Comparator.comparing(charger -> charger.getStation().getStationAddress()))
                    .map(Charger::getId)
                    .collect(Collectors.toList());

        } else {
            return chargers.stream()
                    .sorted(
                            Comparator.comparingDouble(charger -> {
                                Station station = charger.getStation();
                                return calDistance(myLat, myLongi, station.getLatitude(), station.getLongitude());
                            })
                    )
                    .map(Charger::getId)
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
