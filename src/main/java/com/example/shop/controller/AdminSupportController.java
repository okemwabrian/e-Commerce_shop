package com.example.shop.controller;

import com.example.shop.dto.PageResponse;
import com.example.shop.dto.SupportDtos.TicketResponse;
import com.example.shop.dto.SupportDtos.TicketUpdateRequest;
import com.example.shop.model.TicketStatus;
import com.example.shop.service.SupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/support/tickets")
@RequiredArgsConstructor
public class AdminSupportController {
    private final SupportService service;

    @GetMapping
    public PageResponse<TicketResponse> list(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.adminList(status, page, size);
    }

    @PatchMapping("/{id}")
    public TicketResponse update(@PathVariable Long id,
                                 @Valid @RequestBody TicketUpdateRequest request) {
        return service.adminUpdate(id, request);
    }
}
