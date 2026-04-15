package com.br.zetta.fire.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class AlertService {
    private final JavaMailSender mailSender;
    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    //Mensagens padrões alertas
    private static final String SUBJECT = "️ ALERTA CRÍTICO: Risco de Incêndio Detectado";
    private static final String BODY_TEMPLATE = """
            Prezado Usuário,
            
            Detectamos um perigo de incêndio iminente em sua área monitorada pelo sistema Zetta Fire.
            Por favor, siga os protocolos de segurança e evacue o local se necessário.
            
            Este é um alerta automático. Não responda a este e-mail.
            """;

    @Async
    public void sendEmail(String to) {
        try {
            logger.info("Iniciando tenatativa de envio de alerta de incêndio para: {}", to);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(SUBJECT);
            message.setText(BODY_TEMPLATE);

            mailSender.send(message);

            logger.info("Alerta enviado com sucesso para: {}", to);

        } catch (Exception e) {
            logger.error("ERRO CRITICO: Falha ao enviar alerta para {}. Motivo: {}", to, e.getMessage());
        }
    }

    public AlertService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
}
