package com.ecar.servicestation.infra.mail.service;

import com.ecar.servicestation.infra.mail.dto.EmailMessageDto;

public interface MailService {

    void send(EmailMessageDto emailMessage);
}
