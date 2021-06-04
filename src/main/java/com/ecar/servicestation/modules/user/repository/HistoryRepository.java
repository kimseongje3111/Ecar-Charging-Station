package com.ecar.servicestation.modules.user.repository;

import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.History;
import com.ecar.servicestation.modules.user.repository.custom.HistoryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, Long>, HistoryRepositoryCustom {

    boolean existsHistoryByAccountAndStation(Account account, Station station);
}
