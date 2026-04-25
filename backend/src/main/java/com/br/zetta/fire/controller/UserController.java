package com.br.zetta.fire.controller;

import com.br.zetta.fire.data.dto.request.ForgotPasswordDTO;
import com.br.zetta.fire.data.dto.request.ResetPasswordDTO;
import com.br.zetta.fire.data.dto.response.UserResponseDTO;
import com.br.zetta.fire.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {this.userService = userService;}


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordDTO dto) {
        userService.generatePasswordResetToken(dto.email());

        return ResponseEntity.ok("Recovery code sent to your email: " + dto.email());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDTO dto) {
        userService.validateAndChangePassword(dto.code(), dto.newPassword());

        return ResponseEntity.ok("Password changed successfully! You can now log in to Bem-Te-Vi.");
    }


}
