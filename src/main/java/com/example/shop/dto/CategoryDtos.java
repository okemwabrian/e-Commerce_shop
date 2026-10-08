package com.example.shop.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public final class CategoryDtos {
    private CategoryDtos() {
    }

    public record CategoryRequest(@NotBlank String name, Long parentId) {
    }

    public record CategoryResponse(
            Long id,
            String name,
            String slug,
            Long parentId,
            List<CategoryResponse> children
    ) {
    }
}
