package com.ecar.servicestation.modules.ecar.repository;

import com.ecar.servicestation.modules.ecar.domain.Station;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station, Long> {

    Station findStationByStationNumber(long number);
}
