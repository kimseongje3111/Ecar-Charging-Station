package com.ecar.servicestation.modules.user.service;

import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.dto.request.users.UpdateNotificationRequest;
import com.ecar.servicestation.modules.user.dto.request.users.UpdatePasswordRequest;
import com.ecar.servicestation.modules.user.dto.request.users.UpdateUserRequestDto;
import com.ecar.servicestation.modules.user.exception.users.CUserLoginFailedException;
import com.ecar.servicestation.modules.user.exception.users.CUserNotFoundException;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBasicService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Account getLoginUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findAccountByEmail(authentication.getName()).orElseThrow(CUserNotFoundException::new);
    }

    @Transactional
    public void updateUserInfo(UpdateUserRequestDto request) {
        Account account = getLoginUserContext();

        account.setName(request.getUserName());
        account.setPhoneNumber(request.getPhoneNumber());
    }

    @Transactional
    public void updateUserPassword(UpdatePasswordRequest request) {
        Account account = getLoginUserContext();

        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword())) {
            throw new CUserLoginFailedException();
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional
    public void updateUserNotificationSetting(UpdateNotificationRequest request) {
        Account account = getLoginUserContext();

        account.setOnNotificationOfReservationStart(request.isOnNotificationOfReservationStart());
        account.setNotificationMinutesBeforeReservationStart(request.getMinutesBeforeReservationStart());
        account.setOnNotificationOfChargingEnd(request.isOnNotificationOfChargingEnd());
        account.setNotificationMinutesBeforeChargingEnd(request.getMinutesBeforeChargingEnd());
    }

}
