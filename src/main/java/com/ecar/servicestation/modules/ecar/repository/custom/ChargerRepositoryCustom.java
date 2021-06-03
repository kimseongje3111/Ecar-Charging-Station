package com.ecar.servicestation.modules.ecar.repository.custom;

import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.dto.SearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChargerRepositoryCustom {

    Page<Charger> findAllWithStationBySearchConditionAndPaging(List<Long> ids, SearchCondition condition, Pageable pageable);

    Page<Charger> findAllWithStationByPaging(List<Long> ids, Pageable pageable);

    List<Charger> findAllByChargerNumberAndSearchCondition(List<Long> numbers, SearchCondition condition);

    List<Charger> findAllByChargerNumber(List<Long> numbers);
}
