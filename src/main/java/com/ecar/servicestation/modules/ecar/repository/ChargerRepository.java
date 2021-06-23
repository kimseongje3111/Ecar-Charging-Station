package com.ecar.servicestation.modules.ecar.repository;

import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.repository.custom.ChargerRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargerRepository extends JpaRepository<Charger, Long>, ChargerRepositoryCustom {

    Charger findChargerByChargerNumber(long chargerNumber);
}
