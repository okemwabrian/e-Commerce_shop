package com.example.shop.service;

import com.example.shop.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RevokedTokenCleanup {
    private final RevokedTokenRepository revokedTokens;

    @Scheduled(fixedDelayString = "PT1H")
    @Transactional
    public void deleteExpiredTokens() {
        revokedTokens.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
