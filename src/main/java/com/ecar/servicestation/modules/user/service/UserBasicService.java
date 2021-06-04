package com.ecar.servicestation.modules.user.service;

import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.History;
import com.ecar.servicestation.modules.user.dto.UserHistory;
import com.ecar.servicestation.modules.user.exception.CUserNotFoundException;
import com.ecar.servicestation.modules.user.repository.HistoryRepository;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBasicService {

    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;
    private final ModelMapper modelMapper;

    public Account getUserBasicInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findAccountByEmail(authentication.getName()).orElseThrow(CUserNotFoundException::new);
    }

    public List<UserHistory> getUserHistories(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = userRepository.findAccountByEmail(authentication.getName()).orElseThrow(CUserNotFoundException::new);
        List<History> histories = historyRepository.findAllWithStationByAccountAndPaging(account.getId(), pageable).getContent();

        return histories.stream()
                .map(history -> {
                    UserHistory userHistory = modelMapper.map(history, UserHistory.class);
                    userHistory.setChargerCount(history.getStation().getChargers().size());

                    return userHistory;
                })
                .collect(Collectors.toList());
    }
}
