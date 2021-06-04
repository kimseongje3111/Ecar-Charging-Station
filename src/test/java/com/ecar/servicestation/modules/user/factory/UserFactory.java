package com.ecar.servicestation.modules.user.factory;

import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class UserFactory {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Account createSimpleAccount(String userName, String password, String email) {
        Account account =
                Account.builder()
                        .userName(userName)
                        .password(passwordEncoder.encode(password))
                        .email(email)
                        .roles(Collections.singletonList("ROLE_USER"))
                        .build();

        account.generateEmailAuthToken();

        return userRepository.save(account);
    }

    @Transactional
    public Account createVerifiedAccount(String userName, String password, String email) {
        Account account = createSimpleAccount(userName, password, email);
        account.setEmailAuthVerified(true);
        account.setJoinedAt(LocalDateTime.now());

        return account;
    }
}