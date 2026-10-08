package com.example.shop.service;

import com.example.shop.dto.ChatDtos.ChatMessageResponse;
import com.example.shop.dto.ChatDtos.ConversationResponse;
import com.example.shop.exception.BadRequestException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.AppUser;
import com.example.shop.model.ChatMessage;
import com.example.shop.model.NotificationType;
import com.example.shop.model.Role;
import com.example.shop.repository.AppUserRepository;
import com.example.shop.repository.ChatMessageRepository;
import com.example.shop.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    private final ChatMessageRepository repo;
    private final AppUserRepository users;
    private final CurrentUser currentUser;
    private final SimpMessagingTemplate messaging;
    private final NotificationService notifications;

    /** The sender identity comes from the authenticated STOMP principal. */
    public ChatMessageResponse send(String senderEmail, Long customerId, String content) {
        if (content == null || content.isBlank()) {
            throw new BadRequestException("Message is empty");
        }
        if (content.length() > 2000) {
            throw new BadRequestException("Message must be 2000 characters or fewer");
        }

        AppUser sender = users.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean fromSupport = sender.getRole() != Role.CUSTOMER;
        AppUser customer = sender;
        if (fromSupport) {
            if (customerId == null) {
                throw new BadRequestException("customerId is required when replying");
            }
            customer = users.findById(customerId)
                    .filter(user -> user.getRole() == Role.CUSTOMER)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        }

        ChatMessage message = new ChatMessage();
        message.setCustomer(customer);
        message.setSender(sender);
        message.setFromSupport(fromSupport);
        message.setContent(content.trim());
        repo.save(message);

        ChatMessageResponse response = ChatMessageResponse.from(message);
        messaging.convertAndSendToUser(customer.getEmail(), "/queue/chat", response);
        messaging.convertAndSend("/topic/support", response);
        if (fromSupport) {
            String notificationMessage = message.getContent().length() > 80
                    ? message.getContent().substring(0, 80) + "..."
                    : message.getContent();
            notifications.send(customer, NotificationType.SUPPORT, "New message from support",
                    notificationMessage);
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> myHistory() {
        return repo.findByCustomerOrderByCreatedAtAsc(currentUser.require()).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> conversation(Long customerId) {
        AppUser customer = users.findById(customerId)
                .filter(user -> user.getRole() == Role.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return repo.findByCustomerOrderByCreatedAtAsc(customer).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> conversations() {
        return repo.findLatestPerCustomer().stream()
                .map(message -> new ConversationResponse(message.getCustomer().getId(),
                        message.getCustomer().getFullName(), message.getContent(),
                        message.getCreatedAt()))
                .toList();
    }
}
