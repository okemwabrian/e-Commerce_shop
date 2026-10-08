package com.example.shop.repository;

import com.example.shop.model.AppUser;
import com.example.shop.model.SupportTicket;
import com.example.shop.model.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByUserOrderByCreatedAtDesc(AppUser user);

    Optional<SupportTicket> findByIdAndUser(Long id, AppUser user);

    Page<SupportTicket> findByStatus(TicketStatus status, Pageable pageable);

    long countByUserAndStatusIn(AppUser user, Collection<TicketStatus> statuses);
}
