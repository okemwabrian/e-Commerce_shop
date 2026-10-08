package com.example.shop.service;

import com.example.shop.dto.CartDtos.CartItemResponse;
import com.example.shop.dto.CartDtos.CartResponse;
import com.example.shop.exception.BadRequestException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.AppUser;
import com.example.shop.model.Cart;
import com.example.shop.model.CartItem;
import com.example.shop.model.Product;
import com.example.shop.repository.CartRepository;
import com.example.shop.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final CartRepository carts;
    private final ProductService productService;
    private final CurrentUser currentUser;
    private final PricingService pricing;

    public CartResponse getCart() {
        return toResponse(getOrCreate());
    }

    public CartResponse addItem(Long productId, int quantity) {
        if (quantity < 1) {
            throw new BadRequestException("Quantity must be at least 1");
        }
        Product product = productService.getEntity(productId);
        Cart cart = getOrCreate();
        CartItem item = find(cart, productId);
        int newQuantity = (item == null ? 0 : item.getQuantity()) + quantity;
        checkStock(product, newQuantity);
        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            cart.getItems().add(item);
        }
        item.setQuantity(newQuantity);
        return toResponse(cart);
    }

    public CartResponse setQuantity(Long productId, int quantity) {
        Cart cart = getOrCreate();
        CartItem item = find(cart, productId);
        if (item == null) {
            throw new ResourceNotFoundException("That product is not in your cart");
        }
        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            checkStock(item.getProduct(), quantity);
            item.setQuantity(quantity);
        }
        return toResponse(cart);
    }

    public CartResponse removeItem(Long productId) {
        return setQuantity(productId, 0);
    }

    public CartResponse clear() {
        Cart cart = getOrCreate();
        cart.getItems().clear();
        return toResponse(cart);
    }

    private Cart getOrCreate() {
        AppUser user = currentUser.require();
        return carts.findByUser(user).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return carts.save(cart);
        });
    }

    private CartItem find(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    private void checkStock(Product product, int wanted) {
        if (wanted > product.getStock()) {
            throw new BadRequestException("Only " + product.getStock() + " of '"
                    + product.getName() + "' left in stock");
        }
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> lines = cart.getItems().stream().map(item -> {
            Product product = item.getProduct();
            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            return new CartItemResponse(product.getId(), product.getName(), product.getImageUrl(),
                    product.getPrice(), item.getQuantity(), lineTotal, product.getStock());
        }).toList();
        BigDecimal subtotal = lines.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalItems = lines.stream().mapToInt(CartItemResponse::quantity).sum();
        BigDecimal shippingFee = pricing.shipping(subtotal);
        return new CartResponse(lines, totalItems, subtotal, shippingFee,
                subtotal.add(shippingFee));
    }
}
