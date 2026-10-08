package com.example.shop.service;

import com.example.shop.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class WhatsAppService {
    @Value("${shop.whatsapp-number}")
    private String shopNumber;

    public String link(String number, String message) {
        String digits = normalize(number);
        String text = URLEncoder.encode(message, StandardCharsets.UTF_8).replace("+", "%20");
        return "https://wa.me/" + digits + "?text=" + text;
    }

    public String shopLink(String message) {
        return link(shopNumber, message);
    }

    /** Converts Kenyan local 07... phone numbers into international 254... format. */
    public String normalize(String number) {
        if (number == null) {
            throw new BadRequestException("No phone number available");
        }
        String digits = number.replaceAll("[^0-9]", "");
        if (digits.length() == 10 && digits.startsWith("0")) {
            digits = "254" + digits.substring(1);
        }
        if (digits.length() < 9) {
            throw new BadRequestException("Invalid phone number");
        }
        return digits;
    }
}
