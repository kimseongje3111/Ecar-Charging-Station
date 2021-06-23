package com.ecar.servicestation.infra.data.service;

import com.ecar.servicestation.infra.data.dto.EVInfoDto;
import com.ecar.servicestation.infra.data.dto.EVInfoBodyDto;
import com.ecar.servicestation.infra.data.dto.EVInfoHeaderDto;
import com.ecar.servicestation.infra.data.dto.EVInfoResponseDto;
import com.ecar.servicestation.infra.data.exception.EVInfoServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Component
@Profile({"dev","test"})
public class EVInfoService implements ECarChargingStationInfoProvider {

    private static final String RESPONSE_SUCCESS = "00";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ecarapi.servicekey.encoding}")
    private String SERVICE_KEY_ENCODING;

    @Value("${ecarapi.servicekey.decoding}")
    private String SERVICE_KEY_DECODING;

    @Value("${ecarapi.url}")
    private String URL;

    @Override
    public List<EVInfoDto> getData(String search, int page, int numberOfRows) {
        String uri = getURI(search, page, numberOfRows, SERVICE_KEY_DECODING);
        EVInfoResponseDto response = restTemplate.getForObject(uri, EVInfoResponseDto.class);
        EVInfoHeaderDto header = Objects.requireNonNull(response).getHeader();
        EVInfoBodyDto body = Objects.requireNonNull(response).getBody();

        if (!header.getResultCode().equals(RESPONSE_SUCCESS) || body == null) {
            throw new EVInfoServiceException();
        }

        return body.getItems();
    }

    private String getURI(String search, int page, int numberOfRows, String serviceKeyType) {
        StringBuilder stringBuilder = new StringBuilder();

        return stringBuilder
                .append(URL)
                .append("?addr=").append(search)
                .append("&pageNo=").append(page)
                .append("&numOfRows=").append(numberOfRows)
                .append("&ServiceKey=").append(serviceKeyType)
                .toString();
    }
}
