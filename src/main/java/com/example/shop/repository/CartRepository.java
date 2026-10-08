package com.example.shop.repository;

import com.example.shop.model.AppUser;
import com.example.shop.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(AppUser user);
}
