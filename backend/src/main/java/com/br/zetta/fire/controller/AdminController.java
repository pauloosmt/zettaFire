package com.br.zetta.fire.controller;

import com.br.zetta.fire.data.dto.response.UserResponseDTO;
import com.br.zetta.fire.service.FireService;
import com.br.zetta.fire.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("admin")
public class AdminController {

    private final FireService fireService;
    private final UserService userService;

    public AdminController(FireService fireService, UserService userService) {
        this.fireService = fireService;
        this.userService = userService;
    }


    @GetMapping("dashboard/stats")
    public ResponseEntity<Map<String, Object>> getStats(@RequestParam(required = false) String city, @RequestParam(required = false) Integer days) {

        return ResponseEntity.ok().body(fireService.getDashboardStats(city, days));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestParam String email) {
        return ResponseEntity.ok().body(userService.deleteUser(email));
    }

    @GetMapping("/all-users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok().body(userService.getAllUsers());
    }
}
