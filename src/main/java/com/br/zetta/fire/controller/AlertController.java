package com.br.zetta.fire.controller;

import com.br.zetta.fire.data.dto.response.AlertResponse;
import com.br.zetta.fire.service.AlertService;
import com.br.zetta.fire.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("alert")
public class AlertController {
    private final EmailService emailService;

    public AlertController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<AlertResponse> sendAlert(@RequestParam String email) {
        if(email == null || !email.contains("@") ) {
            return ResponseEntity.badRequest().body(new AlertResponse(
                    "Email Inválido",
                    "ERROR",
                    LocalDateTime.now().toString()
            ));
        }

        emailService.sendEmail(email, null);

        AlertResponse response = new AlertResponse("Processo de alerta disparado com suceeso para: " + email,
            "SUCCESS",
            LocalDateTime.now().toString()
        );

        return ResponseEntity.ok(response);
    }
}
