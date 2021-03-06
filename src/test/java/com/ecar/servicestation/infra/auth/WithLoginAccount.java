package com.ecar.servicestation.infra.auth;

import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.factory.UserFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Getter
@RequiredArgsConstructor
public class WithLoginAccount {

    private final AuthTokenProvider authTokenProvider;
    private final UserFactory userFactory;

    private String authToken;
    private Account account;

    public void init() {
        this.account = userFactory.createVerifiedAccount("ADMIN01", "ADMIN01-PASSWORD", "ADMIN01@test.com");
        this.authToken = authTokenProvider.createToken(String.valueOf(account.getId()), account.getRoles());
    }
}
