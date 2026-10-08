package com.example.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class AccountDtos {
    private AccountDtos() {
    }

    public record UpdateProfileRequest(
            @NotBlank String fullName,
            String phone,
            @Pattern(regexp = "^\\+?[0-9]{9,15}$",
                    message = "Enter a valid WhatsApp number, digits only")
            String whatsappNumber,
            boolean marketingConsent
    ) {
    }

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
            String newPassword
    ) {
    }

    public record DeactivateRequest(String password) {
    }

    public record LegalDocument(String title, String version, LocalDate lastUpdated,
                                String content) {
    }
}
