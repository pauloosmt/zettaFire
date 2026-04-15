package com.br.zetta.fire.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AlertService {
    private final JavaMailSender mailSender;

    //Mensagens padrões alertas
    private static final String SUBJECT = "️ ALERTA CRÍTICO: Risco de Incêndio Detectado";
    private static final String BODY_TEMPLATE = """
            Prezado Usuário,
            
            Detectamos um perigo de incêndio iminente em sua área monitorada pelo sistema Zetta Fire.
            Por favor, siga os protocolos de segurança e evacue o local se necessário.
            
            Este é um alerta automático. Não responda a este e-mail.
            """;

    public AlertService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(SUBJECT);
        message.setText(BODY_TEMPLATE);

        mailSender.send(message);
    }
}
