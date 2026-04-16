package com.br.zetta.fire.service;

import com.br.zetta.fire.data.entity.Alert;
import com.br.zetta.fire.data.entity.FireEvent;
import com.br.zetta.fire.data.entity.User;
import com.br.zetta.fire.data.entity.enums.StatusAlert;
import com.br.zetta.fire.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Service
public class AlertService {
    private final AlertRepository alertRepository;
    private final EmailService emailService;

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    public AlertService( AlertRepository alertRepository, EmailService emailService) {
        this.alertRepository = alertRepository;
        this.emailService = emailService;
    }

    //Mensagens padrões alertas
    private static final String SUBJECT = "️ ALERTA CRÍTICO: Risco de Incêndio Detectado";
    private static final String BODY_TEMPLATE = """
            Prezado Usuário,
            
            Detectamos um perigo de incêndio iminente em sua área monitorada pelo sistema Zetta Fire.
            Por favor, siga os protocolos de segurança e evacue o local se necessário.
            
            Este é um alerta automático. Não responda a este e-mail.
            """;

    @Transactional
    public void createAndSendAlerts(FireEvent fireEvent, List<User> usersAtRisk) {
        if(usersAtRisk.isEmpty()) return;

        Alert alert = new Alert();
        alert.setShippingDate(LocalDate.now());
        alert.setStatusAlert(StatusAlert.PENDING);
        alert.setFireEvent(fireEvent);

        alert.setUserList(usersAtRisk);

        Alert savedAlert = alertRepository.save(alert);

        for(User user : usersAtRisk) {
            emailService.sendEmail(user.getEmail(), savedAlert.getIdAlert());
        }
    }


}
