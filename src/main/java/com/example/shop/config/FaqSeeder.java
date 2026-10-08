package com.example.shop.config;

import com.example.shop.model.Faq;
import com.example.shop.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "shop.seed.enabled", havingValue = "true")
public class FaqSeeder implements CommandLineRunner {
    private final FaqRepository faqs;

    @Override
    public void run(String... args) {
        if (faqs.count() > 0) {
            return;
        }
        add("How do I track my order?",
                "Open My Orders and select the order. The status shows where it is.", "Orders");
        add("Can I cancel an order?",
                "Yes, while it is Pending or Confirmed. After it ships, contact support.", "Orders");
        add("Which payment methods do you accept?",
                "Cash on delivery, M-Pesa and card.", "Payments");
        add("How much is delivery?",
                "A flat fee, and free for orders above the free-delivery amount.", "Delivery");
        add("How do I return an item?",
                "Contact support with your order number within the return period.", "Returns");
    }

    private void add(String question, String answer, String category) {
        Faq faq = new Faq();
        faq.setQuestion(question);
        faq.setAnswer(answer);
        faq.setCategory(category);
        faqs.save(faq);
    }
}
