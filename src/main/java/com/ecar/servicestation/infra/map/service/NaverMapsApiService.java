package com.ecar.servicestation.infra.map.service;

import com.ecar.servicestation.infra.map.dto.MapLocationDto;
import com.ecar.servicestation.infra.map.dto.MapRegionDto;
import com.ecar.servicestation.infra.map.dto.MapResponseDto;
import com.ecar.servicestation.infra.map.dto.MapResultDto;
import com.ecar.servicestation.infra.map.exception.MapNotFoundException;
import com.ecar.servicestation.infra.map.exception.MapServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "test"})
public class NaverMapsApiService implements MapService {

    private static final int REVERSE_GEO_RESPONSE_SUCCESS = 0;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${external_api.map.servicekey.x_ncp_apigw_api_key_id}")
    private String X_NCP_APIGW_API_KEY_ID;

    @Value("${external_api.map.servicekey.x_ncp_apigw_api_key}")
    private String X_NCP_APIGW_API_KEY;

    @Value("${external_api.map.base_url.reverse_geo}")
    private String BASE_URL_REVERSE_GEO;

    @Override
    public String reverseGeoCoding(MapLocationDto location) {
        MapResponseDto response =
                webClient
                        .get()
                        .uri(makeReverseGeoCodingServiceUri(location))
                        .header("X-NCP-APIGW-API-KEY-ID", X_NCP_APIGW_API_KEY_ID)
                        .header("X-NCP-APIGW-API-KEY", X_NCP_APIGW_API_KEY)
                        .retrieve()
                        .bodyToMono(MapResponseDto.class)
                        .block();

        if (response.getError() != null) {
            throw new MapServiceException();
        }

        if (response.getStatus().getCode() != REVERSE_GEO_RESPONSE_SUCCESS) {
            throw new MapNotFoundException();
        }

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(response.getResults().get(0).getRegion().getArea1().getName());
        stringBuilder.append(" ");
        stringBuilder.append(response.getResults().get(0).getRegion().getArea2().getName());

        return stringBuilder.toString();
    }

    private String makeReverseGeoCodingServiceUri(MapLocationDto location) {
        StringBuilder stringBuilder = new StringBuilder();

        return stringBuilder
                .append(BASE_URL_REVERSE_GEO)
                .append("?coords=").append(location.getLongitude()).append(",").append(location.getLatitude())
                .append("&orders=legalcode")
                .append("&output=json")
                .toString();
    }
}
