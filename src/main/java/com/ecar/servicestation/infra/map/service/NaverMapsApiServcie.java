package com.ecar.servicestation.infra.map.service;

import com.ecar.servicestation.infra.map.dto.MapLocationDto;
import com.ecar.servicestation.infra.map.exception.MapServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "test"})
public class NaverMapsApiServcie implements MapService {

    private static final String GEO_RESPONSE_SUCCESS = "OK";
    private static final int REVERSE_GEO_RESPONSE_SUCCESS = 0;
    private static final int REVERSE_GEO_RESPONSE_NO_DATA = 3;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mapapi.servicekey.x_ncp_apigw_api_key_id}")
    private String X_NCP_APIGW_API_KEY_ID;

    @Value("${mapapi.servicekey.x_ncp_apigw_api_key}")
    private String X_NCP_APIGW_API_KEY;

    @Value("${mapapi.url.geo}")
    private String GEO_URL;

    @Value("${mapapi.url.reverse_geo}")
    private String REVERSE_GEO_URL;

    @Override
    public MapLocationDto geoCoding(String address) {
        try {
            String uri = getGeoCodingURI(address);
            ResponseEntity<String> entity = restTemplate.exchange(uri, HttpMethod.GET, addCustomHeadersForGeoCoding(), String.class);
            JsonNode jsonNode = objectMapper.readTree(entity.getBody());

            if (jsonNode.has("error") && jsonNode.get("error").get("errorCode").asText().equals("300")) {
                throw new MapServiceException();
            }

            PriorityQueue<MapLocationDto> locations = new PriorityQueue<>(Comparator.comparingDouble(MapLocationDto::getDistance));

            if (jsonNode.get("status").asText().equals(GEO_RESPONSE_SUCCESS)) {
                jsonNode.get("addresses").forEach(node -> {
                    MapLocationDto location = new MapLocationDto();
                    location.setLatitude(node.get("y").asDouble());
                    location.setLongitude(node.get("x").asDouble());
                    location.setDistance(node.get("distance").asDouble());

                    locations.add(location);
                });
            }

            return locations.peek();

        } catch (JsonProcessingException e) {
            log.error("json parsing error", e);

            throw new RuntimeException(e);
        }
    }

    @Override
    public String reverseGeoCoding(MapLocationDto location) {
        String uri = getReverseGeoCodingURI(location.getLatitude(), location.getLongitude());
        ResponseEntity<String> entity = restTemplate.exchange(uri, HttpMethod.GET, addCustomHeadersForReverseGeocoding(), String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(entity.getBody());

            if (jsonNode.has("error") && jsonNode.get("error").get("errorCode").asText().equals("300")) {
                throw new MapServiceException();
            }

            int status = jsonNode.get("status").get("code").asInt();
            StringBuilder stringBuilder = new StringBuilder();

            if (status == REVERSE_GEO_RESPONSE_NO_DATA) {
                throw new MapServiceException();
            }

            if (status == REVERSE_GEO_RESPONSE_SUCCESS) {
                jsonNode.get("results").forEach(node -> {
                    String transMode = node.get("name").asText();
                    JsonNode region = node.get("region");

                    if (transMode.equals("legalcode")) {
                        for (int i = 1; i <= 3; i++) {
                            stringBuilder.append(region.get("area" + i).get("name").asText()).append(" ");
                        }
                    }
                });
            }

            return stringBuilder.toString().trim();

        } catch (JsonProcessingException e) {
            log.error("json parsing error", e);

            throw new RuntimeException(e);
        }
    }

    private String getGeoCodingURI(String address) {
        StringBuilder stringBuilder = new StringBuilder();

        return stringBuilder
                .append(GEO_URL)
                .append("?query=").append(address)
                .append("?count=100")
                .toString();
    }

    private String getReverseGeoCodingURI(double lat, double longi) {
        StringBuilder stringBuilder = new StringBuilder();

        return stringBuilder
                .append(REVERSE_GEO_URL)
                .append("?coords=").append(longi).append(",").append(lat)
                .append("&output=json")
                .toString();
    }

    private HttpEntity addCustomHeadersForGeoCoding() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("X-NCP-APIGW-API-KEY-ID", X_NCP_APIGW_API_KEY_ID);
        headers.set("X-NCP-APIGW-API-KEY", X_NCP_APIGW_API_KEY);

        return new HttpEntity(headers);
    }

    private HttpEntity addCustomHeadersForReverseGeocoding() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", X_NCP_APIGW_API_KEY_ID);
        headers.set("X-NCP-APIGW-API-KEY", X_NCP_APIGW_API_KEY);

        return new HttpEntity(headers);
    }
}
