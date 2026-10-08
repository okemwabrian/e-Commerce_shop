package com.example.shop.security;

import com.example.shop.repository.AppUserRepository;
import com.example.shop.repository.RevokedTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final AppUserRepository users;
    private final RevokedTokenRepository revoked;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.parse(header.substring(7));
                if (!revoked.existsById(claims.getId())) {
                    users.findByEmail(claims.getSubject())
                            .filter(user -> user.isEnabled())
                            .ifPresent(user -> {
                                var authentication = new UsernamePasswordAuthenticationToken(
                                        user.getEmail(), null,
                                        List.of(new SimpleGrantedAuthority(
                                                "ROLE_" + user.getRole().name())));
                                SecurityContextHolder.getContext()
                                        .setAuthentication(authentication);
                            });
                }
            } catch (JwtException | IllegalArgumentException ignored) {
                // Invalid, expired, or revoked tokens remain unauthenticated.
            }
        }
        chain.doFilter(request, response);
    }
}
