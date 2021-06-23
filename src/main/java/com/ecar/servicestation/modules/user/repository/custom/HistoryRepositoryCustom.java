package com.ecar.servicestation.modules.user.repository.custom;

import com.ecar.servicestation.modules.user.domain.History;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HistoryRepositoryCustom {

    Page<History> findAllWithStationByAccountAndPaging(long accountId, Pageable pageable);
}
