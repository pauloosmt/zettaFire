package com.br.zetta.fire.service;

import com.br.zetta.fire.data.entity.Alert;
import com.br.zetta.fire.data.entity.FireEvent;
import com.br.zetta.fire.data.entity.User;
import com.br.zetta.fire.data.entity.enums.StatusAlert;
import com.br.zetta.fire.data.entity.enums.StatusFire;
import com.br.zetta.fire.repository.AlertRepository;
import com.br.zetta.fire.repository.FireEventRepository;
import com.br.zetta.fire.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FireService {
    @Autowired private FireEventRepository fireRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AlertService alertService;

    @Transactional
    public FireEvent createFireEvent(FireEvent event) {
        if (event.getStartTime() == null) {
            event.setStartTime(java.time.LocalDateTime.now());
        }


        if (event.getStatusFire() == null) {
            event.setStatusFire(StatusFire.ACTIVE);
        }

        FireEvent savedEvent = fireRepository.save(event);


        List<User> usersAtRisk = userRepository.findUsersAtRisk(savedEvent.getIdFireEvent());

        if (!usersAtRisk.isEmpty()) {
            alertService.createAndSendAlerts(savedEvent, usersAtRisk);
        }

        return savedEvent;
    }
}
