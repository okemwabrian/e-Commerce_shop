package com.example.shop.service;

import com.example.shop.dto.AccountDtos.LegalDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class LegalService {
    @Value("${shop.support-email}")
    private String supportEmail;

    public LegalDocument privacyPolicy() {
        String content = """
                1. Who we are
                My Shop operates this online store. You can contact us at %s.
                2. What information we collect
                - Account details: name, email address, phone and WhatsApp number, encrypted password.
                - Order details: delivery address, items bought, payment method chosen.
                - Usage details: products you view, wishlist items, messages sent to support.
                3. Why we use it
                To create your account, process and deliver orders, answer your questions,
                keep the shop secure, and, only if you agree, send you offers.
                4. Who we share it with
                Delivery partners and payment providers, only as needed to complete your order.
                We do not sell your personal data.
                5. How long we keep it
                Account and order records are kept as long as needed for your orders and for legal
                and accounting duties. You may close your account at any time.
                6. Your rights
                You may see and correct your data in your account, withdraw marketing consent,
                ask us to deactivate your account, and ask questions about how your data is used.
                7. Security
                Passwords are stored as one-way hashes and traffic is protected in transit.
                8. Changes
                When we change this policy we update the version and date shown above.
                """.formatted(supportEmail);
        return new LegalDocument("Privacy Policy", "1.0", LocalDate.of(2026, 10, 1), content);
    }

    public LegalDocument terms() {
        String content = """
                1. Using the shop: you must give correct information and keep your password private.
                2. Prices and stock: prices are shown in Kenya shillings and may change. An order is
                confirmed only after we accept it.
                3. Delivery: delivery times are estimates.
                4. Returns and refunds: describe your return period and conditions here.
                5. Accounts: we may suspend accounts that break these terms.
                6. Contact: %s
                """.formatted(supportEmail);
        return new LegalDocument("Terms and Conditions", "1.0", LocalDate.of(2026, 10, 1), content);
    }
}
