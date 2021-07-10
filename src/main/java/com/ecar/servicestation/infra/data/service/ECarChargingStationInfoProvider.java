package com.ecar.servicestation.infra.data.service;

import com.ecar.servicestation.infra.data.dto.EVInfoDto;

import java.util.Set;

public interface ECarChargingStationInfoProvider {

    Set<EVInfoDto> getData(String search, int page, int numberOfRows);

}
