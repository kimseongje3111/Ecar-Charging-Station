package com.ecar.servicestation.modules.ecar.repository;

import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.ecar.repository.custom.ReservationTableRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationTableRepository extends JpaRepository<ReservationTable, Long>, ReservationTableRepositoryCustom {
}
