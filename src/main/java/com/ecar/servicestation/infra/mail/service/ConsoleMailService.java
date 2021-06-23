package com.ecar.servicestation.infra.mail.service;

import com.ecar.servicestation.infra.mail.dto.EmailMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile({"local", "test"})
public class ConsoleMailService implements MailService {

    @Override
    public void send(EmailMessageDto emailMessage) {
        log.info("E-mail sent successfully.");
    }
}


