package com.ecar.servicestation.modules.ecar.repository.custom;

import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.dto.request.SearchConditionDto;
import com.ecar.servicestation.modules.ecar.dto.request.SearchLocationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChargerRepositoryCustom {

    Charger findChargerWithStationById(long id);

    Page<Charger> findAllWithStationBySearchConditionAndPaging(List<Long> ids, SearchConditionDto condition, Pageable pageable);

    Page<Charger> findAllWithStationBySearchLocationAndPaging(List<Long> ids, SearchLocationDto location, Pageable pageable);

}
