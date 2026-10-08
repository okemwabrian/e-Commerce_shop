package com.example.shop.controller;

import com.example.shop.dto.ProductDtos.ProductResponse;
import com.example.shop.service.ProductService;
import com.example.shop.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
@RequiredArgsConstructor
public class WhatsAppController {
    private final WhatsAppService whatsApp;
    private final ProductService productService;

    @Value("${shop.frontend-url}")
    private String frontendUrl;

    @GetMapping("/support-link")
    public Map<String, String> support(
            @RequestParam(defaultValue = "Hello, I need help with my order") String message) {
        return Map.of("url", whatsApp.shopLink(message));
    }

    @GetMapping("/share-product/{id}")
    public Map<String, String> shareProduct(@PathVariable Long id) {
        ProductResponse product = productService.get(id);
        String text = "Check this out: " + product.name() + " - "
                + frontendUrl + "/products/" + product.id();
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8).replace("+", "%20");
        return Map.of("url", "https://wa.me/?text=" + encodedText);
    }
}
