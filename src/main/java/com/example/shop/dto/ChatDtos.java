package com.example.shop.dto;

import com.example.shop.model.ChatMessage;

import java.time.LocalDateTime;

public final class ChatDtos {
    private ChatDtos() {
    }

    /** customerId is used only for support replies; customer senders are identified by their token. */
    public record ChatSendRequest(Long customerId, String content) {
    }

    public record ChatMessageResponse(Long id, Long customerId, String senderName,
                                      boolean fromSupport, String content,
                                      LocalDateTime createdAt) {
        public static ChatMessageResponse from(ChatMessage message) {
            return new ChatMessageResponse(message.getId(), message.getCustomer().getId(),
                    message.getSender().getFullName(), message.isFromSupport(),
                    message.getContent(), message.getCreatedAt());
        }
    }

    public record ConversationResponse(Long customerId, String customerName,
                                       String lastMessage, LocalDateTime lastAt) {
    }

    public record InboxSummary(long unreadNotifications, long openTickets,
                               ChatMessageResponse lastChatMessage) {
    }
}
