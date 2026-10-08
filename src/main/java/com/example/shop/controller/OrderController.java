package com.example.shop.controller;

import com.example.shop.dto.OrderDtos.CheckoutRequest;
import com.example.shop.dto.OrderDtos.OrderResponse;
import com.example.shop.dto.PageResponse;
import com.example.shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(@Valid @RequestBody CheckoutRequest request) {
        return service.checkout(request);
    }

    @GetMapping
    public PageResponse<OrderResponse> mine(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.myOrders(q, page, size);
    }

    @GetMapping("/{id}")
    public OrderResponse one(@PathVariable Long id) {
        return service.getMine(id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @GetMapping("/{id}/whatsapp-link")
    public Map<String, String> whatsapp(@PathVariable Long id) {
        return Map.of("url", service.whatsappLink(id));
    }
}
