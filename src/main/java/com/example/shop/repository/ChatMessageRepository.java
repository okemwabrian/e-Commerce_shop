package com.example.shop.repository;

import com.example.shop.model.AppUser;
import com.example.shop.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByCustomerOrderByCreatedAtAsc(AppUser customer);

    Optional<ChatMessage> findFirstByCustomerOrderByCreatedAtDesc(AppUser customer);

    @Query("select m from ChatMessage m where m.id in "
            + "(select max(m2.id) from ChatMessage m2 group by m2.customer.id) "
            + "order by m.createdAt desc")
    List<ChatMessage> findLatestPerCustomer();
}
