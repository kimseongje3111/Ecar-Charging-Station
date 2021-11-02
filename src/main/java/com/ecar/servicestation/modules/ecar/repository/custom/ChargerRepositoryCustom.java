package com.ecar.servicestation.modules.ecar.repository.custom;

import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.dto.request.searchs.SearchConditionDto;
import com.ecar.servicestation.modules.ecar.dto.request.searchs.SearchLocationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChargerRepositoryCustom {

    Charger findChargerWithStationById(long id);

    Page<Charger> findAllWithStationByPaging(List<Long> ids, Pageable pageable);

    Page<Charger> findAllWithStationPaging(List<Long> ids, Pageable pageable);

}
