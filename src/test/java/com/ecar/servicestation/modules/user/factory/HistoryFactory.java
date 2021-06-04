package com.ecar.servicestation.modules.user.factory;

import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.History;
import com.ecar.servicestation.modules.user.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HistoryFactory {

    private final HistoryRepository historyRepository;

    @Transactional
    public History createHistory(Account account, Station station) {
        return historyRepository.save(
                History.builder()
                        .account(account)
                        .station(station)
                        .searchedAt(LocalDateTime.now())
                        .build()
        );
    }
}
