package com.ecar.servicestation.infra.address.service;

import com.ecar.servicestation.infra.address.dto.AddressDto;
import com.ecar.servicestation.infra.address.dto.AddressCommonDto;
import com.ecar.servicestation.infra.address.dto.AddressResponseDto;
import com.ecar.servicestation.infra.address.dto.AddressResultsDto;
import com.ecar.servicestation.infra.address.exception.AddressExceededException;
import com.ecar.servicestation.infra.address.exception.AddressServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "test"})
public class LinkAddressService implements AddressService {

    private static final String RESPONSE_SUCCESS = "0";
    private static final String RESPONSE_SYSTEM_ERROR = "-999";
    private static final String RESPONSE_SERVICE_KEY_ERROR = "E0014";
    private static final int RESPONSE_MAX_RESULTS = 5000;

    private final WebClient webClient;

    @Value("${external_api.address.servicekey.development}")
    private String SERVICE_KEY;

    @Value("${external_api.address.base_url}")
    private String BASE_URL;

    @Override
    public Set<AddressDto> convertToAddressDto(String search, int page, int numberOfRows) {
        AddressResponseDto response =
                webClient
                        .get()
                        .uri(makeServiceUri(search, page, numberOfRows))
                        .retrieve()
                        .bodyToMono(AddressResponseDto.class)
                        .block();

        AddressResultsDto results = Objects.requireNonNull(response).getResults();
        AddressCommonDto common = results.getCommon();
        String errorCode = common.getErrorCode();

        if (!errorCode.equals(RESPONSE_SUCCESS) || results.getJuso().size() == 0) {
            if (errorCode.equals(RESPONSE_SYSTEM_ERROR) || errorCode.equals(RESPONSE_SERVICE_KEY_ERROR)) {
                throw new AddressServiceException();
            }

            return new HashSet<>();
        }

        if (Integer.parseInt(common.getTotalCount()) > RESPONSE_MAX_RESULTS) {
            throw new AddressExceededException();
        }

        Set<AddressDto> address = new HashSet<>(results.getJuso());
        List<Mono<AddressResponseDto>> responseMonoes = new ArrayList<>();

        while (page * numberOfRows < Integer.parseInt(common.getTotalCount())) {
            responseMonoes.add(
                    webClient
                            .get()
                            .uri(makeServiceUri(search, ++page, numberOfRows))
                            .retrieve()
                            .bodyToMono(AddressResponseDto.class)
            );
        }

        if (responseMonoes.size() != 0) {
            List<AddressResponseDto> responses =
                    Mono.zip(responseMonoes, responseMono ->
                            Arrays.stream(responseMono)
                                    .filter(o -> o instanceof AddressResponseDto)
                                    .map(o -> (AddressResponseDto) o)
                                    .collect(Collectors.toList())
                    ).block();

            if (responses != null) {
                responses.stream()
                        .map(result -> result.getResults().getJuso())
                        .forEach(address::addAll);
            }
        }

        return address;
    }

    private String makeServiceUri(String search, int page, int numberOfRows) {
        StringBuilder stringBuilder = new StringBuilder();

        return stringBuilder
                .append(BASE_URL)
                .append("?currentPage=").append(page)
                .append("&countPerPage=").append(numberOfRows)
                .append("&keyword=").append(search)
                .append("&confmKey=").append(SERVICE_KEY)
                .append("&resultType=json")
                .toString();
    }
}
