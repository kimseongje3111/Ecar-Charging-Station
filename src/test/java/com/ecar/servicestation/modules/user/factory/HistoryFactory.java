package com.ecar.servicestation.modules.user.factory;

import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.History;
import com.ecar.servicestation.modules.user.exception.CUserNotFoundException;
import com.ecar.servicestation.modules.user.repository.HistoryRepository;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HistoryFactory {

    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;

    @Transactional
    public History createHistory(Account account, Station station) {
        account = userRepository.findAccountByEmail(account.getEmail()).orElseThrow(CUserNotFoundException::new);

        History history =
                historyRepository.save(
                        History.builder()
                                .account(account)
                                .station(station)
                                .searchedAt(LocalDateTime.now())
                                .build()
                );

        account.addHistory(history);

        return history;
    }
}
