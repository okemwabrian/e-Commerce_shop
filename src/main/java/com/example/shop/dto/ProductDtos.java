package com.example.shop.dto;

import com.example.shop.model.Category;
import com.example.shop.model.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ProductDtos {
    private ProductDtos() {
    }

    public record ProductRequest(
            @NotBlank String name,
            String description,
            @NotNull @Positive BigDecimal price,
            @PositiveOrZero int stock,
            String brand,
            String imageUrl,
            @NotNull Long categoryId
    ) {
    }

    public record ProductResponse(
            Long id,
            String name,
            String description,
            BigDecimal price,
            int stock,
            String brand,
            String imageUrl,
            Long categoryId,
            String categoryName,
            LocalDateTime createdAt
    ) {
        public static ProductResponse from(Product product) {
            Category category = product.getCategory();
            return new ProductResponse(product.getId(), product.getName(), product.getDescription(),
                    product.getPrice(), product.getStock(), product.getBrand(), product.getImageUrl(),
                    category == null ? null : category.getId(),
                    category == null ? null : category.getName(), product.getCreatedAt());
        }
    }

    public record SuggestionResponse(List<String> products, List<String> categories) {
    }
}
