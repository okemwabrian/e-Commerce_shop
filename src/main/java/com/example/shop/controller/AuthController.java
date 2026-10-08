package com.example.shop.controller;

import com.example.shop.dto.AuthDtos.AuthResponse;
import com.example.shop.dto.AuthDtos.GoogleLoginRequest;
import com.example.shop.dto.AuthDtos.LoginRequest;
import com.example.shop.dto.AuthDtos.RegisterRequest;
import com.example.shop.exception.BadRequestException;
import com.example.shop.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/google")
    public AuthResponse google(@Valid @RequestBody GoogleLoginRequest request) {
        return authService.loginWithGoogle(request);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@RequestHeader("Authorization") String authorization) {
        if (!authorization.startsWith("Bearer ") || authorization.length() <= 7) {
            throw new BadRequestException("Authorization must contain a Bearer token");
        }
        authService.logout(authorization.substring(7));
        return Map.of("message", "Logged out");
    }
}
