package com.ecar.servicestation.modules.ecar.service;

import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.ecar.dto.response.ChargerInfoDto;
import com.ecar.servicestation.modules.ecar.dto.response.StationInfoDto;
import com.ecar.servicestation.modules.ecar.exception.CChargerNotFoundException;
import com.ecar.servicestation.modules.ecar.exception.CStationNotFoundException;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.StationRepository;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.History;
import com.ecar.servicestation.modules.user.exception.users.CUserNotFoundException;
import com.ecar.servicestation.modules.user.repository.BookmarkRepository;
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

    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;
    private final BookmarkRepository bookmarkRepository;
    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final ModelMapper modelMapper;

    public StationInfoDto getStationInfo(long stationId) {
        Account account = getLoginUserContext();
        Station station = stationRepository.findById(stationId).orElseThrow(CStationNotFoundException::new);

        StationInfoDto stationInfo = modelMapper.map(station, StationInfoDto.class);
        stationInfo.setStationId(station.getId());
        stationInfo.setBookmarked(bookmarkRepository.existsBookmarkByAccountAndStation(account, station));

        return stationInfo;
    }

    @Transactional
    public StationInfoDto getStationInfoAndRecordHistory(long stationId) {
        Account account = getLoginUserContext();
        Station station = stationRepository.findById(stationId).orElseThrow(CStationNotFoundException::new);
        History history = historyRepository.findHistoryByAccountAndStation(account, station);

        if (history == null) {
            account.addHistory(
                    historyRepository.save(
                            History.builder()
                                    .account(account)
                                    .station(station)
                                    .searchedAt(LocalDateTime.now())
                                    .build()
                    )
            );

        } else {
            history.setSearchedAt(LocalDateTime.now());
        }

        StationInfoDto stationInfo = modelMapper.map(station, StationInfoDto.class);
        stationInfo.setStationId(station.getId());
        stationInfo.setBookmarked(bookmarkRepository.existsBookmarkByAccountAndStation(account, station));

        return stationInfo;
    }

    public ChargerInfoDto getChargerInfo(Long chargerId) {
        Charger charger = chargerRepository.findChargerWithStationById(chargerId);

        if (charger == null) {
            throw new CChargerNotFoundException();
        }

        ChargerInfoDto chargerInfo = modelMapper.map(charger, ChargerInfoDto.class);
        chargerInfo.setChargerId(charger.getId());

        return chargerInfo;
    }

    @Transactional
    public ChargerInfoDto getChargerInfoAndRecordHistory(Long chargerId) {
        Account account = getLoginUserContext();
        Charger charger = chargerRepository.findChargerWithStationById(chargerId);
        History history = historyRepository.findHistoryByAccountAndStation(account, charger.getStation());

        if (history == null) {
            account.addHistory(
                    historyRepository.save(
                            History.builder()
                                    .account(account)
                                    .station(charger.getStation())
                                    .searchedAt(LocalDateTime.now())
                                    .build()
                    )
            );

        } else {
            history.setSearchedAt(LocalDateTime.now());
        }

        ChargerInfoDto chargerInfo = modelMapper.map(charger, ChargerInfoDto.class);
        chargerInfo.setChargerId(charger.getId());

        return chargerInfo;
    }

    private Account getLoginUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findAccountByEmail(authentication.getName()).orElseThrow(CUserNotFoundException::new);
    }

}
