package com.ecar.servicestation.infra.map;

import com.ecar.servicestation.infra.map.exception.ReverseGeoCodingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class NaverMapsApiServcie implements MapService {

    @Value("${mapapi.servicekey.x_ncp_apigw_api_key_id}")
    private String X_NCP_APIGW_API_KEY_ID;

    @Value("${mapapi.servicekey.x_ncp_apigw_api_key}")
    private String X_NCP_APIGW_API_KEY;

    @Value("${mapapi.url}")
    private String URL;

    private final int RESPONSE_SUCCESS = 0;
    private final int RESPONSE_NO_DATA = 3;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Override
    public String reverseGeoCoding(long lat, long lang) {
        ResponseEntity<String> entity = restTemplate.exchange(getURI(lat, lang), HttpMethod.GET, addCustomHeaders(), String.class);

        try {
            StringBuilder stringBuilder = new StringBuilder();
            JsonNode jsonNode = objectMapper.readTree(entity.getBody());
            int status = jsonNode.get("status").get("code").asInt();

            if (status == RESPONSE_NO_DATA) {
                throw new ReverseGeoCodingException();
            }

            if (status == RESPONSE_SUCCESS) {
                Iterator<JsonNode> elements = jsonNode.get("results").elements();

                while (!elements.hasNext()) {
                    JsonNode next = elements.next();

                    if (next.get("name").asText().equals("legalcode")) {
                        for (int i = 1; i <= 4; i++) {
                            String area = next.get("region").get("area" + i).get("name").asText();

                            if (!area.isEmpty()) {
                                stringBuilder.append(area).append(" ");
                            }
                        }

                        break;
                    }
                }
            }

            return stringBuilder.toString().trim();

        } catch (JsonProcessingException e) {
            log.error("json parsing error", e);

            throw new RuntimeException(e);
        }
    }

    private String getURI(long lat, long lang) {
        StringBuilder stringBuilder = new StringBuilder();

        return stringBuilder
                .append(URL)
                .append("?coords=").append(lang).append(",").append(lat)
                .append("&output=json")
                .toString();
    }

    private HttpEntity addCustomHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", X_NCP_APIGW_API_KEY_ID);
        headers.set("X-NCP-APIGW-API-KEY", X_NCP_APIGW_API_KEY);

        return new HttpEntity(headers);
    }
}
