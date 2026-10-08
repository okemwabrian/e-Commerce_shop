package com.example.shop.repository;

import com.example.shop.model.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ProductSpecs {
    private ProductSpecs() {
    }

    public static Specification<Product> filter(String query, List<Long> categoryIds,
                                                BigDecimal min, BigDecimal max) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> conditions = new ArrayList<>();
            conditions.add(criteriaBuilder.isTrue(root.get("active")));

            if (query != null && !query.isBlank()) {
                conditions.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + query.trim().toLowerCase() + "%"));
            }
            if (categoryIds != null && !categoryIds.isEmpty()) {
                conditions.add(root.get("category").get("id").in(categoryIds));
            }
            if (min != null) {
                conditions.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), min));
            }
            if (max != null) {
                conditions.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), max));
            }
            return criteriaBuilder.and(conditions.toArray(new Predicate[0]));
        };
    }
}
