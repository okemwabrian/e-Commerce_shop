package com.example.shop.service;

import com.example.shop.dto.ProductDtos.ProductResponse;
import com.example.shop.model.AppUser;
import com.example.shop.model.Product;
import com.example.shop.model.RecentlyViewed;
import com.example.shop.repository.RecentlyViewedRepository;
import com.example.shop.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RecentlyViewedService {
    private final RecentlyViewedRepository repo;
    private final ProductService productService;
    private final CurrentUser currentUser;

    public void record(Long productId) {
        AppUser user = currentUser.optional().orElse(null);
        if (user == null) {
            return;
        }
        Product product = productService.getEntity(productId);
        RecentlyViewed viewed = repo.findByUserAndProduct(user, product).orElseGet(() -> {
            RecentlyViewed newView = new RecentlyViewed();
            newView.setUser(user);
            newView.setProduct(product);
            return newView;
        });
        viewed.setViewedAt(LocalDateTime.now());
        repo.save(viewed);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list() {
        return repo.findTop20ByUserOrderByViewedAtDesc(currentUser.require()).stream()
                .map(viewed -> ProductResponse.from(viewed.getProduct()))
                .toList();
    }

    public void clear() {
        repo.deleteByUser(currentUser.require());
    }
}
