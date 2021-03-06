package com.ecar.servicestation.modules.user.service;

import com.ecar.servicestation.infra.app.AppProperties;
import com.ecar.servicestation.infra.auth.AuthTokenProvider;
import com.ecar.servicestation.infra.mail.dto.EmailMessageDto;
import com.ecar.servicestation.infra.mail.service.MailService;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.DeviceToken;
import com.ecar.servicestation.modules.user.dto.request.users.EmailAuthRequestDto;
import com.ecar.servicestation.modules.user.dto.request.users.LoginRequestDto;
import com.ecar.servicestation.modules.user.dto.request.users.SignUpRequestDto;
import com.ecar.servicestation.modules.user.exception.users.CUserLoginFailedException;
import com.ecar.servicestation.modules.user.exception.users.CUserNotFoundException;
import com.ecar.servicestation.modules.user.exception.users.CUserSignUpFailedException;
import com.ecar.servicestation.modules.user.repository.DeviceTokenRepository;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserLoginAndSignUpService {

    private final UserRepository userRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final AuthTokenProvider authTokenProvider;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    @Transactional
    public Account signUp(SignUpRequestDto request) {
        if (userRepository.existsAccountByEmail(request.getEmail())) {
            throw new CUserSignUpFailedException();
        }

        Account newAccount =
                Account.builder()
                        .name(request.getUserName())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .email(request.getEmail())
                        .phoneNumber(request.getPhoneNumber())
                        .roles(Collections.singletonList("ROLE_USER"))
                        .build();

        newAccount.generateEmailAuthToken();

        return userRepository.save(newAccount);
    }

    public void sendEmailForAuthentication(Account account) {
        EmailMessageDto emailMessage =
                EmailMessageDto.builder()
                        .to(account.getEmail())
                        .subject("[????????? ????????? ?????? ???] ?????? ?????? ????????? ?????? ?????? ?????? ???????????????.")
                        .text(getContentOfAuthenticationEmail(account))
                        .build();

        mailService.send(emailMessage);
    }

    @Transactional
    public void validateEmailAuthToken(EmailAuthRequestDto request) {
        Account account = findAccountByEmail(request.getEmail());

        if (request.getToken().equals(account.getEmailAuthToken())) {
            account.successEmailAuthentication();
        }
    }

    public String login(LoginRequestDto request) {
        Account account = findAccountByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword()) || !account.isEmailAuthVerified()) {
            throw new CUserLoginFailedException();
        }

        // ???????????? ?????? ?????? //

        DeviceToken deviceToken = deviceTokenRepository.findDeviceTokenByAccount(account);

        if (deviceToken == null) {
            deviceTokenRepository.save(
                    DeviceToken.builder()
                            .deviceUniqueToken(request.getDeviceToken())
                            .account(account)
                            .build()
            );

        } else {
            deviceToken.setDeviceUniqueToken(request.getDeviceToken());
        }

        // ????????? ?????????, ?????? token ?????? //

        return authTokenProvider.createToken(String.valueOf(account.getId()), account.getRoles());
    }

    private Account findAccountByEmail(String email) {
        return userRepository.findAccountByEmail(email).orElseThrow(CUserNotFoundException::new);
    }

    private String getContentOfAuthenticationEmail(Account account) {
        Context context = new Context();
        context.setVariable("messageTitle", "????????? ????????? ?????? ?????? ????????? ???????????? ????????? ??????????????????.");
        context.setVariable("messageContent", "?????? ????????? ??????????????? ?????? ????????? ??? ??? ????????????.");
        context.setVariable("userName", account.getName());
        context.setVariable("email", account.getEmail());
        context.setVariable("token", account.getEmailAuthToken());
        context.setVariable("link", appProperties.getHost() + "/email-auth-token");

        return templateEngine.process("simple-email-template", context);
    }

}
