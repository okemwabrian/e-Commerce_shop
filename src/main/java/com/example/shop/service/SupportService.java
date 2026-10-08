package com.example.shop.service;

import com.example.shop.dto.PageResponse;
import com.example.shop.dto.SupportDtos.ContactInfo;
import com.example.shop.dto.SupportDtos.FaqResponse;
import com.example.shop.dto.SupportDtos.TicketRequest;
import com.example.shop.dto.SupportDtos.TicketResponse;
import com.example.shop.dto.SupportDtos.TicketUpdateRequest;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.NotificationType;
import com.example.shop.model.SupportTicket;
import com.example.shop.model.TicketStatus;
import com.example.shop.repository.FaqRepository;
import com.example.shop.repository.SupportTicketRepository;
import com.example.shop.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupportService {
    private final FaqRepository faqs;
    private final SupportTicketRepository tickets;
    private final CurrentUser currentUser;
    private final NotificationService notifications;
    private final WhatsAppService whatsApp;

    @Value("${shop.support-email}")
    private String email;

    @Value("${shop.support-phone}")
    private String phone;

    @Value("${shop.support-hours}")
    private String hours;

    @Transactional(readOnly = true)
    public List<FaqResponse> faqs() {
        return faqs.findAllByOrderByIdAsc().stream().map(FaqResponse::from).toList();
    }

    public ContactInfo contact() {
        return new ContactInfo(email, phone, hours, whatsApp.shopLink("Hello, I need help"));
    }

    public TicketResponse create(TicketRequest request) {
        SupportTicket ticket = new SupportTicket();
        ticket.setUser(currentUser.require());
        ticket.setSubject(request.subject().trim());
        ticket.setMessage(request.message().trim());
        return TicketResponse.from(tickets.save(ticket));
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> myTickets() {
        return tickets.findByUserOrderByCreatedAtDesc(currentUser.require()).stream()
                .map(TicketResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse myTicket(Long id) {
        return TicketResponse.from(tickets.findByIdAndUser(id, currentUser.require())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found")));
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> adminList(TicketStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SupportTicket> result = status == null
                ? tickets.findAll(pageable)
                : tickets.findByStatus(status, pageable);
        return PageResponse.from(result.map(TicketResponse::from));
    }

    public TicketResponse adminUpdate(Long id, TicketUpdateRequest request) {
        SupportTicket ticket = tickets.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        ticket.setStatus(request.status());
        if (request.response() != null && !request.response().isBlank()) {
            ticket.setAdminResponse(request.response().trim());
        }
        ticket.setUpdatedAt(LocalDateTime.now());
        notifications.send(ticket.getUser(), NotificationType.SUPPORT,
                "Update on your support request", "Your ticket '" + ticket.getSubject()
                        + "' is now " + ticket.getStatus() + ".");
        return TicketResponse.from(ticket);
    }
}
