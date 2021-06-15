package com.ecar.servicestation.modules.ecar.service;

import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.ecar.dto.response.StationInfo;
import com.ecar.servicestation.modules.ecar.exception.CChargerNotFoundException;
import com.ecar.servicestation.modules.ecar.exception.CStationNotFoundException;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.StationRepository;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.History;
import com.ecar.servicestation.modules.user.exception.CUserNotFoundException;
import com.ecar.servicestation.modules.user.repository.HistoryRepository;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ECarBasicService {

    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;
    private final ModelMapper modelMapper;

    public StationInfo getStationInfo(long id) {
        Station station = stationRepository.findById(id).orElseThrow(CStationNotFoundException::new);

        return modelMapper.map(station, StationInfo.class);
    }

    public Charger getChargerInfo(Long id) {
        Charger charger = chargerRepository.findChargerWithStationById(id);

        if (charger == null) {
            throw new CChargerNotFoundException();
        }

        return charger;
    }

    @Transactional
    public StationInfo getChargerInfoAndSaveHistory(long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = userRepository.findAccountByEmail(authentication.getName()).orElseThrow(CUserNotFoundException::new);
        Station station = stationRepository.findById(id).orElseThrow(CStationNotFoundException::new);

        account.addHistory(
                historyRepository.save(
                        History.builder()
                                .account(account)
                                .station(station)
                                .searchedAt(LocalDateTime.now())
                                .build()
                )
        );

        return modelMapper.map(station, StationInfo.class);
    }
}
