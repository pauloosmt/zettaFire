package com.br.zetta.fire.service;

import com.br.zetta.fire.data.entity.Alert;
import com.br.zetta.fire.data.entity.FireEvent;
import com.br.zetta.fire.data.entity.User;
import com.br.zetta.fire.data.entity.enums.StatusAlert;
import com.br.zetta.fire.exceptions.custom.AlertProcessingException;
import com.br.zetta.fire.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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


    @Transactional
    public void createAndSendAlerts(FireEvent fireEvent, List<User> usersAtRisk, String SUBJECT, String BODY) {
        if(usersAtRisk.isEmpty()) return;

        Alert alert = new Alert();
        alert.setShippingDate(LocalDate.now());
        alert.setStatusAlert(StatusAlert.PENDING);
        alert.setFireEvent(fireEvent);

        alert.setUserList(usersAtRisk);

        Alert savedAlert = alertRepository.save(alert);

        for(User user : usersAtRisk) {
            emailService.sendEmail(user.getEmail(), savedAlert.getIdAlert(), SUBJECT, BODY);
        }
    }

    @Scheduled(fixedDelay = 20000)
    @Transactional(readOnly = true)
    public void processPendingAlerts() {

        logger.info("Total de alertas no banco: {}", alertRepository.count());
        // Busca alertas com status PENDING (criados pelo Trigger SQL)
        List<Alert> pendingAlerts = alertRepository.findByStatusAlert(StatusAlert.PENDING);

        logger.info("Alertas pendentes encontrados: {}", pendingAlerts.size());

        for (Alert alert : pendingAlerts) {
            try {
                // Pega a lista de usuários em risco vinculada a este alerta
                List<User> usersAtRisk = alert.getUserList();

                for (User user : usersAtRisk) {
                    emailService.sendEmail(
                            user.getEmail(),
                            alert.getIdAlert(),
                            "ALERTA CRÍTICO: Risco de Incêndio Detectado",
                            "Prezado " + user.getName() + ",\n" +
                                    "\n" +
                                    "O sistema Bem-Te-Vi detectou um foco de incêndio ou risco iminente em sua área monitorada.\n" +
                                    "Por favor, mantenha a calma, siga os protocolos de segurança da sua região e, se necessário, realize a evacuação do local imediatamente. A sua segurança e a preservação do meio ambiente são nossas prioridades.\n" +
                                    "\n" +
                                    "Este é um alerta automático. Não responda a este e-mail"
                    );
                }

                // Opcional: Marcar como enviado aqui também para evitar duplicidade enquanto o @Async processa
                alert.setStatusAlert(StatusAlert.SENT);
                alertRepository.save(alert);

            } catch (AlertProcessingException ex) {
                logger.warn("Erro de processamento: {}", ex.getMessage());
                alert.setStatusAlert(StatusAlert.FAILED);
                alertRepository.save(alert);
            }
        }
    }

}
