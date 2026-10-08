package com.example.shop.security;

import com.example.shop.model.AppUser;
import com.example.shop.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CurrentUser {
    private final AppUserRepository users;

    public AppUser require() {
        return optional().orElseThrow(() -> new BadCredentialsException("Please log in"));
    }

    public Optional<AppUser> optional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return users.findByEmail(authentication.getName());
    }
}
