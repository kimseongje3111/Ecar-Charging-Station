package com.ecar.servicestation.infra.data;

import com.ecar.servicestation.infra.data.dto.EVInfo;

import java.util.List;

public interface ECarChargingStationInfoProvider {

    List<EVInfo> getData(String search, int page, int numberOfRows);

}
