package com.ecar.servicestation.modules.user.repository;

import com.ecar.servicestation.modules.user.domain.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, Long> {
}
