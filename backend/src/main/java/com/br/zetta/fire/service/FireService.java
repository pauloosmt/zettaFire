package com.br.zetta.fire.service;


import com.br.zetta.fire.data.entity.FireEvent;
import com.br.zetta.fire.data.entity.User;
import com.br.zetta.fire.data.entity.enums.StatusFire;
import com.br.zetta.fire.repository.AlertRepository;
import com.br.zetta.fire.repository.FireEventRepository;
import com.br.zetta.fire.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FireService {
    @Autowired private FireEventRepository fireRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AlertService alertService;

    private static final String SUBJECT = "️ ALERTA CRÍTICO: Risco de Incêndio Detectado ";
    private static final String BODY_TEMPLATE = """
            Prezado Usuário,
            
            O sistema Bem-Te-Vi detectou um foco de incêndio ou risco iminente em sua área monitorada.
            Por favor, mantenha a calma, siga os protocolos de segurança da sua região e, se necessário, realize a evacuação do local imediatamente. A sua segurança e a preservação do meio ambiente são nossas prioridades.
            
            Este é um alerta automático. Não responda a este e-mail
            """;

    @Transactional
    public FireEvent createFireEvent(FireEvent event) {
        if (event.getIdFocoBdq() != null) {
            var fireEventExistente = fireRepository.findByIdFocoBdq(event.getIdFocoBdq());
            if (fireEventExistente.isPresent()) {
                return fireEventExistente.get();
            }
        }

        if (event.getStartTime() == null) {
            event.setStartTime(java.time.LocalDateTime.now());
        }

        if (event.getStatusFire() == null) {
            event.setStatusFire(StatusFire.ACTIVE);
        }

        FireEvent savedEvent = fireRepository.save(event);


        List<User> usersAtRisk = userRepository.findUsersAtRisk(savedEvent.getIdFireEvent());

        if (!usersAtRisk.isEmpty()) {
            alertService.createAndSendAlerts(savedEvent, usersAtRisk, SUBJECT, BODY_TEMPLATE);
        }

        return savedEvent;
    }

    public Map<String, Object> getDashboardStats(String city, Integer days) {


        int filterDays = (days != null) ? days: 30;
        LocalDateTime startDate = LocalDateTime.now().minusDays(filterDays);

        Long total = fireRepository.countByLocationAndPeriod(city, startDate);

        Map<String, Object> response = new HashMap<>();
        response.put("location", (city != null) ? city : "Minas Gerais");
        response.put("periodDays", filterDays);
        response.put("totalIncidents", total);

        return response;
    }
}
