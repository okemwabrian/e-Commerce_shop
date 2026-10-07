package com.example.shop.service;

import com.example.shop.model.Product;
import com.example.shop.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repository;
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }
    public List<Product> findAll() { return repository.findAll(); }
    public Product findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: "
                        + id));
    }
    public Product save(Product product) { return repository.save(product); }
    public void delete(Long id) { repository.deleteById(id); }
    public List<Product> search(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }
}
