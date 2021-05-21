package com.ecar.servciestation.infra.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
public class ConsoleMailService implements MailService {

    @Override
    public void send(EmailMessage emailMessage) {
        log.info("E-mail sent successfully.");
    }
}


