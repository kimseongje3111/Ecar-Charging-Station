package com.ecar.servicestation.infra.data;

import com.ecar.servicestation.infra.data.dto.EVInfo;
import com.ecar.servicestation.infra.data.dto.EVInfoBody;
import com.ecar.servicestation.infra.data.dto.EVInfoHeader;
import com.ecar.servicestation.infra.data.dto.EVInfoResponse;
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

    @Value("${ecarapi.servicekey.encoding}")
    private String SERVICE_KEY_ENCODING;

    @Value("${ecarapi.servicekey.decoding}")
    private String SERVICE_KEY_DECODING;

    @Value("${ecarapi.url}")
    private String URL;

    private final String RESPONSE_SUCCESS = "00";
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<EVInfo> getData(String search, int page, int numberOfRows) {
        String uri = getURI(search, page, numberOfRows, SERVICE_KEY_DECODING);
        EVInfoResponse response = restTemplate.getForObject(uri, EVInfoResponse.class);
        EVInfoHeader header = Objects.requireNonNull(response).getHeader();
        EVInfoBody body = Objects.requireNonNull(response).getBody();

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
