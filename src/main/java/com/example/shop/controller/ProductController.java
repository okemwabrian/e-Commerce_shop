package com.example.shop.controller;

import com.example.shop.model.Product;
import com.example.shop.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service) {
        this.service = service;
    }

 @GetMapping
 public List<Product> all(@RequestParam(required = false) String search) {
        return (search == null)? service.findAll() : service.search(search);

 }
 @GetMapping("{id}")
    public Product one(@PathVariable Long id) {
        return service.findById(id);
 }
 @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create  (@Valid @RequestBody Product product) {
        return service.save(product);
 }
 @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @Valid @RequestBody Product product) {
        product.setId(id);
        return service.save(product);
 }
 @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
 }

}
