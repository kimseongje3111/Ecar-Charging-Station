package com.ecar.servicestation.modules.ecar.repository.custom;

import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.dto.SearchCondition;

import java.util.List;

public interface ChargerRepositoryCustom {

    List<Charger> findAllWithStationBySearchCondition(List<Long> ids, SearchCondition condition);

    List<Charger> findAllWithStation(List<Long> ids);
}
