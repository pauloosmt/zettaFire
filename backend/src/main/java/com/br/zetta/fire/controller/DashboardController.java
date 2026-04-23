package com.br.zetta.fire.controller;

import com.br.zetta.fire.service.FireService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("admin/dashboard")
public class DashboardController {

    private final FireService fireService;

    public DashboardController(FireService fireService) {
        this.fireService = fireService;
    }


    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(@RequestParam(required = false) String city, @RequestParam(required = false) Integer days) {

        return ResponseEntity.ok().body(fireService.getDashboardStats(city, days));
    }

}
