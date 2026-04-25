package com.br.zetta.fire.service;


import com.br.zetta.fire.data.dto.response.FireEventResponseDTO;
import com.br.zetta.fire.data.entity.FireEvent;
import com.br.zetta.fire.data.entity.User;
import com.br.zetta.fire.data.entity.enums.StatusFire;
import com.br.zetta.fire.repository.AlertRepository;
import com.br.zetta.fire.repository.FireEventRepository;
import com.br.zetta.fire.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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

    public Page<FireEventResponseDTO> listAll(Pageable pageable) {
        return fireRepository.findAll(pageable)
                .map(FireEventResponseDTO::new);
    }

}
