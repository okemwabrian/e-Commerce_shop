package com.example.shop.security;

import com.example.shop.model.AppUser;
import com.example.shop.repository.AppUserRepository;
import com.example.shop.repository.RevokedTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;
    private final AppUserRepository users;
    private final RevokedTokenRepository revoked;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
                StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())
                && accessor.getDestination() != null
                && accessor.getDestination().startsWith("/topic/support")) {
            Authentication authentication = (Authentication) accessor.getUser();
            boolean supportAgent = authentication != null
                    && authentication.getAuthorities().stream().anyMatch(authority ->
                    authority.getAuthority().equals("ROLE_ADMIN")
                            || authority.getAuthority().equals("ROLE_SUPPORT"));
            if (!supportAgent) {
                throw new MessagingException("Not allowed to subscribe to support topics");
            }
        }
        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ") || header.length() <= 7) {
            throw new MessagingException("Missing Bearer token");
        }

        try {
            Claims claims = jwtService.parse(header.substring(7));
            if (revoked.existsById(claims.getId())) {
                throw new MessagingException("Token revoked");
            }
            AppUser user = users.findByEmail(claims.getSubject())
                    .filter(AppUser::isEnabled)
                    .orElseThrow(() -> new MessagingException("Unknown or disabled user"));
            accessor.setUser(new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))));
        } catch (JwtException | IllegalArgumentException exception) {
            throw new MessagingException("Invalid token", exception);
        }
    }
}
