package com.example.shop.controller;

import com.example.shop.dto.ProductDtos.ProductResponse;
import com.example.shop.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService service;

    @GetMapping
    public List<ProductResponse> list() {
        return service.list();
    }

    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@PathVariable Long productId) {
        service.add(productId);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long productId) {
        service.remove(productId);
    }

    @PostMapping("/{productId}/move-to-cart")
    public void moveToCart(@PathVariable Long productId) {
        service.moveToCart(productId);
    }
}
