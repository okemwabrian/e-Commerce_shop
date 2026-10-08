package com.example.shop.controller;

import com.example.shop.dto.CartDtos.AddItemRequest;
import com.example.shop.dto.CartDtos.CartResponse;
import com.example.shop.dto.CartDtos.QuantityRequest;
import com.example.shop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public CartResponse get() {
        return cartService.getCart();
    }

    @PostMapping("/items")
    public CartResponse add(@Valid @RequestBody AddItemRequest request) {
        return cartService.addItem(request.productId(), request.quantity());
    }

    @PutMapping("/items/{productId}")
    public CartResponse setQuantity(@PathVariable Long productId,
                                    @Valid @RequestBody QuantityRequest request) {
        return cartService.setQuantity(productId, request.quantity());
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse remove(@PathVariable Long productId) {
        return cartService.removeItem(productId);
    }

    @DeleteMapping
    public CartResponse clear() {
        return cartService.clear();
    }
}
