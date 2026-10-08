package com.example.shop.controller;

import com.example.shop.dto.AccountDtos.LegalDocument;
import com.example.shop.service.LegalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legal")
@RequiredArgsConstructor
public class LegalController {
    private final LegalService legal;

    @GetMapping("/privacy-policy")
    public LegalDocument privacy() {
        return legal.privacyPolicy();
    }

    @GetMapping("/terms")
    public LegalDocument terms() {
        return legal.terms();
    }
}
