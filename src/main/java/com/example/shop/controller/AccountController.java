package com.example.shop.controller;

import com.example.shop.dto.AccountDtos.ChangePasswordRequest;
import com.example.shop.dto.AccountDtos.DeactivateRequest;
import com.example.shop.dto.AccountDtos.UpdateProfileRequest;
import com.example.shop.dto.AuthDtos.UserResponse;
import com.example.shop.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService service;

    @GetMapping("/me")
    public UserResponse me() {
        return service.me();
    }

    @PutMapping("/me")
    public UserResponse update(@Valid @RequestBody UpdateProfileRequest request) {
        return service.updateProfile(request);
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        service.changePassword(request);
        return Map.of("message", "Password changed");
    }

    @PostMapping("/deactivate")
    public Map<String, String> deactivate(@RequestBody DeactivateRequest request) {
        service.deactivate(request);
        return Map.of("message", "Account deactivated");
    }
}
