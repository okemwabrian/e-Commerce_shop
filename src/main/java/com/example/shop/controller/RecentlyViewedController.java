package com.example.shop.controller;

import com.example.shop.dto.ProductDtos.ProductResponse;
import com.example.shop.service.RecentlyViewedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recently-viewed")
@RequiredArgsConstructor
public class RecentlyViewedController {
    private final RecentlyViewedService service;

    @GetMapping
    public List<ProductResponse> list() {
        return service.list();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear() {
        service.clear();
    }
}
