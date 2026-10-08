package com.example.shop.controller;

import com.example.shop.dto.ChatDtos.ChatSendRequest;
import com.example.shop.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {
    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void send(@Payload ChatSendRequest request, Principal principal) {
        chatService.send(principal.getName(), request.customerId(), request.content());
    }
}
