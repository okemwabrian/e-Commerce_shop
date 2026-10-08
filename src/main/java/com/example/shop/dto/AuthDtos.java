package com.example.shop.dto;

import com.example.shop.model.AppUser;
import com.example.shop.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
            String password,
            String phone
    ) {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    public record GoogleLoginRequest(@NotBlank String idToken) {
    }

    public record UserResponse(Long id, String fullName, String email, String phone,
                               String whatsappNumber, Role role, boolean marketingConsent) {
        public static UserResponse from(AppUser user) {
            return new UserResponse(user.getId(), user.getFullName(), user.getEmail(),
                    user.getPhone(), user.getWhatsappNumber(), user.getRole(),
                    user.isMarketingConsent());
        }
    }

    public record AuthResponse(String token, String tokenType, long expiresInMs,
                               UserResponse user) {
    }
}
