package com.example.shop.service;

import com.example.shop.dto.ProductDtos.ProductRequest;
import com.example.shop.dto.ProductDtos.ProductResponse;
import com.example.shop.dto.ProductDtos.SuggestionResponse;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.Product;
import com.example.shop.repository.CategoryRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.ProductSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String query, Long categoryId, BigDecimal min,
                                        BigDecimal max, Pageable pageable) {
        List<Long> categoryIds = categoryId == null
                ? null
                : categoryService.idsWithChildren(categoryId);
        return productRepository
                .findAll(ProductSpecs.filter(query, categoryIds, min, max), pageable)
                .map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(getEntity(id));
    }

    /** Hidden products count as missing to callers such as carts and orders. */
    @Transactional(readOnly = true)
    public Product getEntity(Long id) {
        return productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getEntity(id);
        apply(product, request);
        return ProductResponse.from(product);
    }

    public void deactivate(Long id) {
        getEntity(id).setActive(false);
    }

    @Transactional(readOnly = true)
    public SuggestionResponse suggestions(String query) {
        if (query == null || query.trim().length() < 2) {
            return new SuggestionResponse(List.of(), List.of());
        }

        String trimmedQuery = query.trim();
        List<String> products = productRepository
                .findTop8ByActiveTrueAndNameContainingIgnoreCase(trimmedQuery)
                .stream()
                .map(Product::getName)
                .toList();
        List<String> categories = categoryRepository
                .findTop5ByNameContainingIgnoreCase(trimmedQuery)
                .stream()
                .map(category -> category.getName())
                .toList();
        return new SuggestionResponse(products, categories);
    }

    private void apply(Product product, ProductRequest request) {
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setBrand(request.brand());
        product.setImageUrl(request.imageUrl());
        product.setCategory(categoryService.getEntity(request.categoryId()));
    }
}
