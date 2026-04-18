package com.br.zetta.fire.controller;

import com.br.zetta.fire.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {this.userService = userService;}

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestParam String email) {
        return ResponseEntity.ok().body(userService.deleteUser(email));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        userService.generatePasswordResetToken(email);

        return ResponseEntity.ok("Recovery code sent to your email: " + email);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String code, @RequestParam String newPassword) {
        userService.validateAndChangePassword(code, newPassword);

        return ResponseEntity.ok("Password changed successfully! You can now log in to Bem-Te-Vi.");
    }
}
