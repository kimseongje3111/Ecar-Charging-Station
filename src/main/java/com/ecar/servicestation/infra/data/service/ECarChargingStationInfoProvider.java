package com.ecar.servicestation.infra.data.service;

import com.ecar.servicestation.infra.data.dto.EVInfoDto;

import java.util.List;

public interface ECarChargingStationInfoProvider {

    List<EVInfoDto> getData(String search, int page, int numberOfRows);

}
