package com.br.zetta.fire.controller;

import com.br.zetta.fire.data.entity.FireEvent;
import com.br.zetta.fire.service.FireService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("fire")
public class FireController {
    private final FireService fireService;

    public FireController(FireService fireService) {
        this.fireService = fireService;
    }

    @PostMapping("/report")
    public ResponseEntity<FireEvent> reportFire(@RequestBody FireEvent event) {
        FireEvent newEvent = fireService.createFireEvent(event);
        return ResponseEntity.ok(newEvent);
    }
}