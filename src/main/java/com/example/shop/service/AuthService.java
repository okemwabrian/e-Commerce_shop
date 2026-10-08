package com.example.shop.service;

import com.example.shop.dto.AuthDtos.AuthResponse;
import com.example.shop.dto.AuthDtos.GoogleLoginRequest;
import com.example.shop.dto.AuthDtos.LoginRequest;
import com.example.shop.dto.AuthDtos.RegisterRequest;
import com.example.shop.dto.AuthDtos.UserResponse;
import com.example.shop.exception.BadRequestException;
import com.example.shop.model.AppUser;
import com.example.shop.model.NotificationType;
import com.example.shop.model.RevokedToken;
import com.example.shop.repository.AppUserRepository;
import com.example.shop.repository.RevokedTokenRepository;
import com.example.shop.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final AppUserRepository users;
    private final RevokedTokenRepository revoked;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final GoogleAuthService googleAuthService;
    private final NotificationService notifications;

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new BadRequestException("This email is already registered");
        }

        AppUser user = new AppUser();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(request.phone());
        user.setPassword(encoder.encode(request.password()));
        users.save(user);
        notifications.send(user, NotificationType.SYSTEM, "Welcome to the shop",
                "Thanks for joining us. Happy shopping!");
        return build(user);
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = users.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!user.isEnabled() || !encoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return build(user);
    }

    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        var payload = googleAuthService.verify(request.idToken());
        if (payload.getEmail() == null || payload.getEmail().isBlank()) {
            throw new BadCredentialsException("Google account did not provide an email");
        }
        String email = payload.getEmail().trim().toLowerCase();
        AppUser user = users.findByEmail(email).orElseGet(() -> {
            AppUser newUser = new AppUser();
            newUser.setEmail(email);
            Object name = payload.get("name");
            newUser.setFullName(name == null ? email.substring(0, email.indexOf('@'))
                    : String.valueOf(name));
            newUser.setPassword(encoder.encode(UUID.randomUUID().toString()));
            newUser.setAuthProvider("GOOGLE");
            return users.save(newUser);
        });
        if (!user.isEnabled()) {
            throw new BadCredentialsException("This account is deactivated");
        }
        return build(user);
    }

    public void logout(String token) {
        Claims claims = jwtService.parse(token);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                claims.getExpiration().toInstant(), ZoneId.systemDefault());
        revoked.save(new RevokedToken(claims.getId(), expiresAt));
    }

    private AuthResponse build(AppUser user) {
        return new AuthResponse(jwtService.generate(user), "Bearer",
                jwtService.getExpirationMs(), UserResponse.from(user));
    }
}
