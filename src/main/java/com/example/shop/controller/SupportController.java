package com.example.shop.controller;

import com.example.shop.dto.SupportDtos.ContactInfo;
import com.example.shop.dto.SupportDtos.FaqResponse;
import com.example.shop.dto.SupportDtos.TicketRequest;
import com.example.shop.dto.SupportDtos.TicketResponse;
import com.example.shop.service.SupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {
    private final SupportService service;

    @GetMapping("/faqs")
    public List<FaqResponse> faqs() {
        return service.faqs();
    }

    @GetMapping("/contact")
    public ContactInfo contact() {
        return service.contact();
    }

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@Valid @RequestBody TicketRequest request) {
        return service.create(request);
    }

    @GetMapping("/tickets")
    public List<TicketResponse> mine() {
        return service.myTickets();
    }

    @GetMapping("/tickets/{id}")
    public TicketResponse one(@PathVariable Long id) {
        return service.myTicket(id);
    }
}
