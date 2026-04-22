package com.br.zetta.fire.service;


import com.br.zetta.fire.data.entity.enums.StatusAlert;
import com.br.zetta.fire.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final AlertRepository alertRepository;


    public EmailService(JavaMailSender mailSender, AlertRepository alertRepository) {
        this.mailSender = mailSender;
        this.alertRepository = alertRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);


    @Async
    public void sendEmail(String to, UUID idAlert, String SUBJECT, String BODY_TEMPLATE) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("Bem-Te-Vi <${spring.mail.username}>");
            message.setTo(to);
            message.setSubject(SUBJECT);
            message.setText(BODY_TEMPLATE);

            mailSender.send(message);

            if (idAlert != null) {
                updateAlertStatus(idAlert, StatusAlert.SENT);
            }
            updateAlertStatus(idAlert, StatusAlert.SENT);
            logger.info("E-mail disparado com sucesso para: {}", to);
        } catch (Exception e) {
            if (idAlert != null) {
                updateAlertStatus(idAlert, StatusAlert.FAILED);
            }
            logger.error("Erro ao enviar email", e);
        }
    }
    private void updateAlertStatus(UUID idAlert, StatusAlert status) {
        if(idAlert == null) return;

        alertRepository.findById(idAlert).ifPresent(alert -> {
            alert.setStatusAlert(status);
            alertRepository.save(alert);
        });
    }
}
