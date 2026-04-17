package com.br.zetta.fire.controller;


import com.br.zetta.fire.data.dto.request.AuthRequestDTO;
import com.br.zetta.fire.data.dto.request.UserRequestDTO;
import com.br.zetta.fire.data.dto.response.LoginResponseDTO;
import com.br.zetta.fire.data.dto.response.UserResponseDTO;
import com.br.zetta.fire.data.entity.User;
import com.br.zetta.fire.infra.security.TokenService;
import com.br.zetta.fire.repository.UserRepository;
import com.br.zetta.fire.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("auth")
public class AuthController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService, UserRepository userRepository, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthRequestDTO data) {
        var userPass = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(userPass);
        var token = tokenService.generateToken((User) Objects.requireNonNull(auth.getPrincipal()));

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody @Valid UserRequestDTO data) {
        if(this.userRepository.findByEmail(data.email()) != null) {
            return ResponseEntity.badRequest().build();
        }
        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());


        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(data, encryptedPassword));
    }
}
