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
    private static final String SUBJECT = "️ ALERTA CRÍTICO: Risco de Incêndio Detectado";
    private static final String BODY_TEMPLATE = """
            Prezado Usuário,
            
            Detectamos um perigo de incêndio iminente em sua área monitorada pelo sistema Zetta Fire.
            Por favor, siga os protocolos de segurança e evacue o local se necessário.
            
            Este é um alerta automático. Não responda a este e-mail.
            """;

    @Async
    public void sendEmail(String to, UUID idAlert) {
        try{
            logger.info("Iniciando tentativa de envio para: {}", to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(SUBJECT);
            message.setText(BODY_TEMPLATE);

            mailSender.send(message);

            updateAlertStatus(idAlert, StatusAlert.SENT);

            logger.info("Alerta enviado com sucesso para: {}", to);
        } catch (Exception e) {
            updateAlertStatus(idAlert, StatusAlert.FAILED);
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
