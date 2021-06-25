package com.ecar.servicestation.modules.ecar.repository;

import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.ecar.repository.custom.ReservationRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<ReservationTable, Long>, ReservationRepositoryCustom {
}
