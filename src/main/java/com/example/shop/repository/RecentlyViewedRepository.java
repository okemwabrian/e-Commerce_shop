package com.example.shop.repository;

import com.example.shop.model.AppUser;
import com.example.shop.model.Product;
import com.example.shop.model.RecentlyViewed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecentlyViewedRepository extends JpaRepository<RecentlyViewed, Long> {
    List<RecentlyViewed> findTop20ByUserOrderByViewedAtDesc(AppUser user);

    Optional<RecentlyViewed> findByUserAndProduct(AppUser user, Product product);

    void deleteByUser(AppUser user);
}
