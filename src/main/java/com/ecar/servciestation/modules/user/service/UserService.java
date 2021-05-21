package com.ecar.servciestation.modules.user.service;

import com.ecar.servciestation.infra.AppProperties;
import com.ecar.servciestation.infra.auth.AuthTokenProvider;
import com.ecar.servciestation.infra.mail.EmailMessage;
import com.ecar.servciestation.infra.mail.MailService;
import com.ecar.servciestation.modules.user.domain.Account;
import com.ecar.servciestation.modules.user.dto.LoginRequestDto;
import com.ecar.servciestation.modules.user.dto.SignUpRequestDto;
import com.ecar.servciestation.modules.user.exception.CUserLoginFailedException;
import com.ecar.servciestation.modules.user.exception.CUserNotFoundException;
import com.ecar.servciestation.modules.user.exception.CUserSignUpFailedException;
import com.ecar.servciestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AuthTokenProvider authTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    public Account findByEmail(String email) {
        Optional<Account> account = userRepository.findAccountByEmail(email);

        return account.orElseThrow(CUserNotFoundException::new);
    }

    public String login(LoginRequestDto request) {      // 로그인 성공시, 인증 token 발급
        Account account = findByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword()) || !account.isEmailAuthVerified()) {
            throw new CUserLoginFailedException();
        }

        return authTokenProvider.createToken(String.valueOf(account.getId()), account.getRoles());
    }

    @Transactional
    public Account signUp(SignUpRequestDto request) {
        if (userRepository.existsAccountByEmail(request.getEmail())) {
            throw new CUserSignUpFailedException();
        }

        Account newAccount =
                Account.builder()
                        .userName(request.getUserName())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .email(request.getEmail())
                        .roles(Collections.singletonList("ROLE_USER"))
                        .build();

        newAccount.generateEmailAuthToken();

        return userRepository.save(newAccount);
    }

    public void sendEmailForAuthentication(Account account) {
        EmailMessage emailMessage =
                EmailMessage.builder()
                        .to(account.getEmail())
                        .subject("[전기차 충전소 알림 앱] 회원 가입 완료를 위한 계정 인증 메일입니다.")
                        .text(getContentOfAuthenticationEmail(account))
                        .build();

        mailService.send(emailMessage);
    }

    @Transactional
    public void validateEmailAuthToken(String email, String token) {
        Account account = findByEmail(email);

        if (token.equals(account.getEmailAuthToken())) {
            account.successEmailAuthentication();
        }
    }

    private String getContentOfAuthenticationEmail(Account account) {
        Context context = new Context();
        context.setVariable("messageTitle", "서비스 이용을 위해 아래 링크를 클릭하여 인증을 완료해주세요.");
        context.setVariable("messageContent", "계정 인증을 완료했다면 이제 로그인 할 수 있습니다.");
        context.setVariable("userName", account.getUsername());
        context.setVariable("email", account.getEmail());
        context.setVariable("token", account.getEmailAuthToken());
        context.setVariable("link", appProperties.getHost() + "/user/email-auth-token");

        return templateEngine.process("simple-email-template", context);
    }
}
