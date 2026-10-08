package com.example.shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {
    private CartDtos() {
    }

    public record AddItemRequest(@NotNull Long productId, @Min(1) int quantity) {
    }

    public record QuantityRequest(@Min(0) int quantity) {
    }

    public record CartItemResponse(Long productId, String name, String imageUrl,
                                    BigDecimal unitPrice, int quantity, BigDecimal lineTotal,
                                    int stockAvailable) {
    }

    public record CartResponse(List<CartItemResponse> items, int totalItems,
                                BigDecimal subtotal, BigDecimal shippingFee, BigDecimal total) {
    }
}
