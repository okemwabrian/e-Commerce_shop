package com.example.shop.controller;

import com.example.shop.dto.ChatDtos.ChatMessageResponse;
import com.example.shop.dto.ChatDtos.ConversationResponse;
import com.example.shop.dto.ChatDtos.InboxSummary;
import com.example.shop.service.ChatService;
import com.example.shop.service.InboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final InboxService inboxService;

    @GetMapping("/api/chat/history")
    public List<ChatMessageResponse> history() {
        return chatService.myHistory();
    }

    @GetMapping("/api/inbox/summary")
    public InboxSummary inbox() {
        return inboxService.summary();
    }

    @GetMapping("/api/admin/chat/conversations")
    public List<ConversationResponse> conversations() {
        return chatService.conversations();
    }

    @GetMapping("/api/admin/chat/{customerId}")
    public List<ChatMessageResponse> conversation(@PathVariable Long customerId) {
        return chatService.conversation(customerId);
    }
}
