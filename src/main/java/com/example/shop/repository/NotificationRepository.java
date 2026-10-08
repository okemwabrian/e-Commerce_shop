package com.example.shop.repository;

import com.example.shop.model.AppUser;
import com.example.shop.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserOrderByCreatedAtDesc(AppUser user, Pageable pageable);

    long countByUserAndSeenFalse(AppUser user);

    Optional<Notification> findByIdAndUser(Long id, AppUser user);

    @Modifying
    @Query("update Notification n set n.seen = true where n.user = :user and n.seen = false")
    int markAllSeen(@Param("user") AppUser user);
}
