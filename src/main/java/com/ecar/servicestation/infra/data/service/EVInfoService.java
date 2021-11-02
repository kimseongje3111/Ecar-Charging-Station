package com.ecar.servicestation.infra.data.service;

import com.ecar.servicestation.infra.data.dto.EVInfoDto;
import com.ecar.servicestation.infra.data.dto.EVInfoBodyDto;
import com.ecar.servicestation.infra.data.dto.EVInfoHeaderDto;
import com.ecar.servicestation.infra.data.dto.EVInfoResponseDto;
import com.ecar.servicestation.infra.data.exception.EVINfoNotFoundException;
import com.ecar.servicestation.infra.data.exception.EVInfoServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Profile({"dev", "test"})
public class EVInfoService implements ECarChargingStationInfoProvider {

    private static final String RESPONSE_SUCCESS = "00";

    private final WebClient webClient;

    @Value("${external_api.ecar.servicekey.decoding}")
    private String SERVICE_KEY_DECODING;

    @Value("${external_api.ecar.base_url}")
    private String BASE_URL;

    @Override
    public Set<EVInfoDto> getData(String search, int page, int numberOfRows) {
        EVInfoResponseDto response =
                webClient
                        .get()
                        .uri(makeServiceUri(search, page, numberOfRows))
                        .retrieve()
                        .bodyToMono(EVInfoResponseDto.class)
                        .block();

        EVInfoHeaderDto header = Objects.requireNonNull(response).getHeader();
        EVInfoBodyDto body = response.getBody();

        if (!header.getResultCode().equals(RESPONSE_SUCCESS) || body == null) {
            throw new EVInfoServiceException();
        }

        if (body.getItems().size() == 0) {
            return new HashSet<>();
        }

        Set<EVInfoDto> results = new HashSet<>(body.getItems());
        List<Mono<EVInfoResponseDto>> responseMonoes = new ArrayList<>();

        while (page * numberOfRows < body.getTotalCount()) {
            responseMonoes.add(
                    webClient
                            .get()
                            .uri(makeServiceUri(search, ++page, numberOfRows))
                            .retrieve()
                            .bodyToMono(EVInfoResponseDto.class)
            );
        }

        if (responseMonoes.size() != 0) {
            List<EVInfoResponseDto> responses =
                    Mono.zip(responseMonoes, responseMono ->
                            Arrays.stream(responseMono)
                                    .filter(o -> o instanceof EVInfoResponseDto)
                                    .map(o -> (EVInfoResponseDto) o)
                                    .collect(Collectors.toList())
                    ).block();

            if (responses != null) {
                responses.stream()
                        .map(result -> result.getBody().getItems())
                        .forEach(results::addAll);
            }
        }

        return results;
    }

    private String makeServiceUri(String search, int page, int numberOfRows) {
        StringBuilder stringBuilder = new StringBuilder();

        return stringBuilder
                .append(BASE_URL)
                .append("?serviceKey=").append(SERVICE_KEY_DECODING)
                .append("&pageNo=").append(page)
                .append("&numOfRows=").append(numberOfRows)
                .append("&addr=").append(search)
                .toString();
    }

}
