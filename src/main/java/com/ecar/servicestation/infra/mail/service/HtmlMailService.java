package com.ecar.servicestation.infra.mail.service;

import com.ecar.servicestation.infra.mail.dto.EmailMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class HtmlMailService implements MailService {

    private final JavaMailSender javaMailSender;

    @Async
    @Override
    public void send(EmailMessageDto emailMessage) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(emailMessage.getTo());
            mimeMessageHelper.setSubject(emailMessage.getSubject());
            mimeMessageHelper.setText(emailMessage.getText(), true);

            javaMailSender.send(mimeMessage);
            log.info("E-mail sent successfully.");

        } catch (MessagingException e) {
            log.error("E-mail sending failed", e);

            throw new RuntimeException(e);
        }
    }

}


