package com.ecar.servciestation.modules.user.service;

import com.ecar.servciestation.modules.user.domain.Account;
import com.ecar.servciestation.modules.user.exception.CUserNotFoundException;
import com.ecar.servciestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userPk) {
        Optional<Account> account = userRepository.findById(Long.valueOf(userPk));

        return account.orElseThrow(CUserNotFoundException::new);
    }
}
