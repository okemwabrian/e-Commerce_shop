package com.example.shop.service;

import com.example.shop.dto.ChatDtos.ChatMessageResponse;
import com.example.shop.dto.ChatDtos.InboxSummary;
import com.example.shop.model.AppUser;
import com.example.shop.model.TicketStatus;
import com.example.shop.repository.ChatMessageRepository;
import com.example.shop.repository.NotificationRepository;
import com.example.shop.repository.SupportTicketRepository;
import com.example.shop.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InboxService {
    private final CurrentUser currentUser;
    private final NotificationRepository notifications;
    private final SupportTicketRepository tickets;
    private final ChatMessageRepository chats;

    public InboxSummary summary() {
        AppUser user = currentUser.require();
        long unread = notifications.countByUserAndSeenFalse(user);
        long openTickets = tickets.countByUserAndStatusIn(user,
                List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS));
        ChatMessageResponse lastMessage = chats.findFirstByCustomerOrderByCreatedAtDesc(user)
                .map(ChatMessageResponse::from)
                .orElse(null);
        return new InboxSummary(unread, openTickets, lastMessage);
    }
}
