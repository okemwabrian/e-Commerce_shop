package com.example.shop.dto;

import com.example.shop.model.OrderItem;
import com.example.shop.model.OrderStatus;
import com.example.shop.model.PaymentMethod;
import com.example.shop.model.ShopOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class OrderDtos {
    private OrderDtos() {
    }

    public record CheckoutRequest(
            @NotBlank String shippingName,
            @NotBlank String shippingPhone,
            @NotBlank String shippingAddress,
            @NotBlank String shippingCity,
            @NotNull PaymentMethod paymentMethod,
            String notes
    ) {
    }

    public record StatusRequest(@NotNull OrderStatus status) {
    }

    public record OrderItemResponse(Long productId, String productName, String imageUrl,
                                     BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(item.getProduct().getId(), item.getProductName(),
                    item.getProduct().getImageUrl(), item.getUnitPrice(), item.getQuantity(),
                    item.getLineTotal());
        }
    }

    public record OrderResponse(Long id, String orderNumber, OrderStatus status,
                                PaymentMethod paymentMethod, BigDecimal subtotal,
                                BigDecimal shippingFee, BigDecimal total, String shippingName,
                                String shippingPhone, String shippingAddress, String shippingCity,
                                LocalDateTime createdAt, List<OrderItemResponse> items) {
        public static OrderResponse from(ShopOrder order) {
            return new OrderResponse(order.getId(), order.getOrderNumber(), order.getStatus(),
                    order.getPaymentMethod(), order.getSubtotal(), order.getShippingFee(),
                    order.getTotal(), order.getShippingName(), order.getShippingPhone(),
                    order.getShippingAddress(), order.getShippingCity(), order.getCreatedAt(),
                    order.getItems().stream().map(OrderItemResponse::from).toList());
        }
    }
}
