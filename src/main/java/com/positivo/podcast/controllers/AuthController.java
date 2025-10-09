package com.positivo.podcast.controllers;

import com.positivo.podcast.dtos.request.LoginRequestDto;
import com.positivo.podcast.dtos.request.RegisterRequestDto;
import com.positivo.podcast.dtos.response.AuthResponseDto;
import com.positivo.podcast.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        String token = authService.login(loginRequestDto);
        return ResponseEntity.ok(new AuthResponseDto(token));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequestDto registerRequestDto) {
        authService.register(registerRequestDto);
        return ResponseEntity.status(201).build(); // Retorna 201 Created
    }
}
