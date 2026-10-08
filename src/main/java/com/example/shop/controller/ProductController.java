package com.example.shop.controller;

import com.example.shop.dto.PageResponse;
import com.example.shop.dto.ProductDtos.ProductRequest;
import com.example.shop.dto.ProductDtos.ProductResponse;
import com.example.shop.dto.ProductDtos.SuggestionResponse;
import com.example.shop.service.ProductService;
import com.example.shop.service.RecentlyViewedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final RecentlyViewedService recentlyViewedService;

    @GetMapping
    public PageResponse<ProductResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return PageResponse.from(
                productService.search(q, categoryId, minPrice, maxPrice, pageable));
    }

    @GetMapping("/{id}")
    public ProductResponse one(@PathVariable Long id) {
        ProductResponse product = productService.get(id);
        recentlyViewedService.record(id);
        return product;
    }

    @GetMapping("/suggestions")
    public SuggestionResponse suggestions(@RequestParam String q) {
        return productService.suggestions(q);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id,
                                  @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.deactivate(id);
    }
}
