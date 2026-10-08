package com.example.shop.dto;

import com.example.shop.model.Faq;
import com.example.shop.model.SupportTicket;
import com.example.shop.model.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class SupportDtos {
    private SupportDtos() {
    }

    public record FaqResponse(Long id, String question, String answer, String category) {
        public static FaqResponse from(Faq faq) {
            return new FaqResponse(faq.getId(), faq.getQuestion(), faq.getAnswer(),
                    faq.getCategory());
        }
    }

    public record ContactInfo(String email, String phone, String hours, String whatsappLink) {
    }

    public record TicketRequest(@NotBlank @Size(max = 200) String subject,
                                @NotBlank @Size(max = 2000) String message) {
    }

    public record TicketUpdateRequest(@NotNull TicketStatus status,
                                      @Size(max = 2000) String response) {
    }

    public record TicketResponse(Long id, String subject, String message, TicketStatus status,
                                 String adminResponse, String customerName,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static TicketResponse from(SupportTicket ticket) {
            return new TicketResponse(ticket.getId(), ticket.getSubject(), ticket.getMessage(),
                    ticket.getStatus(), ticket.getAdminResponse(), ticket.getUser().getFullName(),
                    ticket.getCreatedAt(), ticket.getUpdatedAt());
        }
    }
}
