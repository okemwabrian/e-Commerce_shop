package com.example.shop.repository;

import com.example.shop.model.AppUser;
import com.example.shop.model.OrderStatus;
import com.example.shop.model.ShopOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<ShopOrder, Long> {
    @Query(value = "select distinct o from ShopOrder o left join o.items i "
            + "where o.user = :user and (lower(o.orderNumber) like :q "
            + "or lower(i.productName) like :q)",
            countQuery = "select count(distinct o) from ShopOrder o left join o.items i "
                    + "where o.user = :user and (lower(o.orderNumber) like :q "
                    + "or lower(i.productName) like :q)")
    Page<ShopOrder> search(@Param("user") AppUser user, @Param("q") String query,
                           Pageable pageable);

    Optional<ShopOrder> findByIdAndUser(Long id, AppUser user);

    Page<ShopOrder> findByStatus(OrderStatus status, Pageable pageable);
}
