package com.example.shop.service;

import com.example.shop.dto.ProductDtos.ProductResponse;
import com.example.shop.model.AppUser;
import com.example.shop.model.Product;
import com.example.shop.model.WishlistItem;
import com.example.shop.repository.WishlistRepository;
import com.example.shop.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {
    private final WishlistRepository wishlist;
    private final ProductService productService;
    private final CartService cartService;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<ProductResponse> list() {
        return wishlist.findByUserOrderByAddedAtDesc(currentUser.require()).stream()
                .map(item -> ProductResponse.from(item.getProduct()))
                .toList();
    }

    public void add(Long productId) {
        AppUser user = currentUser.require();
        Product product = productService.getEntity(productId);
        if (wishlist.findByUserAndProduct(user, product).isEmpty()) {
            WishlistItem item = new WishlistItem();
            item.setUser(user);
            item.setProduct(product);
            wishlist.save(item);
        }
    }

    public void remove(Long productId) {
        AppUser user = currentUser.require();
        Product product = productService.getEntity(productId);
        wishlist.findByUserAndProduct(user, product).ifPresent(wishlist::delete);
    }

    public void moveToCart(Long productId) {
        cartService.addItem(productId, 1);
        remove(productId);
    }
}
