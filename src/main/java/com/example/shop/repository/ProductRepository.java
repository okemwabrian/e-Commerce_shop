package com.example.shop.repository;

import com.example.shop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);

}
//we dont have SQL, no implentation, we can save the products,find, delete and alsso serch