package com.ecar.servicestation.infra.address.service;

import com.ecar.servicestation.infra.address.dto.AddressDto;
import com.ecar.servicestation.infra.address.dto.AddressCommonDto;
import com.ecar.servicestation.infra.address.dto.AddressResponseDto;
import com.ecar.servicestation.infra.address.dto.AddressResultsDto;
import com.ecar.servicestation.infra.address.exception.AddressServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "test"})
public class LinkAddressService implements AddressService {

    private static final String RESPONSE_SUCCESS = "0";
    private static final int API_REQUEST_MAX_COUNT = 10;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${addressapi.servicekey.development}")
    private String SERVICE_KEY;

    @Value("${addressapi.url}")
    private String URL;

    @Override
    public Set<AddressDto> convertRoadAddress(String search, int page, int numberOfRows) {
        try {
            Set<AddressDto> addresses = new HashSet<>();

            String uri = getURI(search, page, numberOfRows);
            AddressResponseDto response = restTemplate.getForObject(uri, AddressResponseDto.class);
            AddressResultsDto results = Objects.requireNonNull(response).getResults();
            AddressCommonDto common = results.getCommon();

            if (!common.getErrorCode().equals(RESPONSE_SUCCESS)) {
                throw new AddressServiceException();
            }

            if (results.getJuso().size() == 0) {
                throw new AddressServiceException();
            }

            while (page * numberOfRows <= Integer.parseInt(common.getTotalCount()) && page < API_REQUEST_MAX_COUNT) {
                addresses.addAll(results.getJuso());

                uri = getURI(search, ++page, numberOfRows);
                response = restTemplate.getForObject(uri, AddressResponseDto.class);
                results = Objects.requireNonNull(response).getResults();
            }

            addresses.addAll(results.getJuso());

            return addresses;

        } catch (UnsupportedEncodingException e) {
            log.error("encoding error", e);

            throw new RuntimeException(e);
        }
    }

    private String getURI(String search, int page, int numberOfRows) throws UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();

        return stringBuilder
                .append(URL)
                .append("?currentPage=").append(page)
                .append("&countPerPage=").append(numberOfRows)
                .append("&keyword=").append(search)
                .append("&confmKey=").append(SERVICE_KEY)
                .append("&resultType=json")
                .toString();
    }
}
