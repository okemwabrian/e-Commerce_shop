package com.example.shop.service;

import com.example.shop.dto.AccountDtos.ChangePasswordRequest;
import com.example.shop.dto.AccountDtos.DeactivateRequest;
import com.example.shop.dto.AccountDtos.UpdateProfileRequest;
import com.example.shop.dto.AuthDtos.UserResponse;
import com.example.shop.exception.BadRequestException;
import com.example.shop.model.AppUser;
import com.example.shop.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {
    private final CurrentUser currentUser;
    private final PasswordEncoder encoder;

    @Transactional(readOnly = true)
    public UserResponse me() {
        return UserResponse.from(currentUser.require());
    }

    public UserResponse updateProfile(UpdateProfileRequest request) {
        AppUser user = currentUser.require();
        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone());
        user.setWhatsappNumber(request.whatsappNumber());
        user.setMarketingConsent(request.marketingConsent());
        return UserResponse.from(user);
    }

    public void changePassword(ChangePasswordRequest request) {
        AppUser user = currentUser.require();
        if (!"LOCAL".equals(user.getAuthProvider())) {
            throw new BadRequestException(
                    "Your account uses Google sign-in, so it has no password to change");
        }
        if (!encoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPassword(encoder.encode(request.newPassword()));
    }

    public void deactivate(DeactivateRequest request) {
        AppUser user = currentUser.require();
        if ("LOCAL".equals(user.getAuthProvider())
                && (request.password() == null
                || !encoder.matches(request.password(), user.getPassword()))) {
            throw new BadRequestException("Password is incorrect");
        }
        user.setEnabled(false);
    }
}
