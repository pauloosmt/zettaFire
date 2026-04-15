package com.br.zetta.fire.controller;

import com.br.zetta.fire.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("alert")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendAlert(@RequestParam String email) {
        if(email == null || !email.contains("@") ) {
            return ResponseEntity.badRequest().body("Email inválido");
        }

        alertService.sendEmail(email);

        return ResponseEntity.ok("Processo de alerta iniciado para: " + email);
    }
}
