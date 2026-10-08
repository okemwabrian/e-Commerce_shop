package com.example.shop.repository;

import com.example.shop.model.AppUser;
import com.example.shop.model.Product;
import com.example.shop.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUserOrderByAddedAtDesc(AppUser user);

    Optional<WishlistItem> findByUserAndProduct(AppUser user, Product product);
}
