package com.ecar.servicestation.infra.data;

import com.ecar.servicestation.infra.data.dto.EVInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Profile({"local", "test"})
public class ConsoleEVInfoService implements ECarChargingStationInfoProvider {

    @Override
    public List<EVInfo> getData(String search, int page, int numberOfRows) {
        log.info("Data request is successful.");

        return new ArrayList<>();
    }
}
