package com.example.shop.service;

import com.example.shop.dto.AuthDtos.AuthResponse;
import com.example.shop.dto.AuthDtos.LoginRequest;
import com.example.shop.dto.AuthDtos.RegisterRequest;
import com.example.shop.dto.AuthDtos.UserResponse;
import com.example.shop.exception.BadRequestException;
import com.example.shop.model.AppUser;
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

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final AppUserRepository users;
    private final RevokedTokenRepository revoked;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

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
