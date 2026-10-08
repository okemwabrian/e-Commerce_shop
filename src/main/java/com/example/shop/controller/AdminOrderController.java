package com.example.shop.controller;

import com.example.shop.dto.OrderDtos.OrderResponse;
import com.example.shop.dto.OrderDtos.StatusRequest;
import com.example.shop.dto.PageResponse;
import com.example.shop.model.OrderStatus;
import com.example.shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
    private final OrderService service;

    @GetMapping
    public PageResponse<OrderResponse> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.adminList(status, page, size);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id,
                                      @Valid @RequestBody StatusRequest request) {
        return service.updateStatus(id, request.status());
    }

    @GetMapping("/{id}/customer-whatsapp")
    public Map<String, String> customerWhatsapp(@PathVariable Long id) {
        return Map.of("url", service.customerWhatsappLink(id));
    }
}
