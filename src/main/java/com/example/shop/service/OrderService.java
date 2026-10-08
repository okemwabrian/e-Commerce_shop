package com.example.shop.service;

import com.example.shop.dto.OrderDtos.CheckoutRequest;
import com.example.shop.dto.OrderDtos.OrderResponse;
import com.example.shop.dto.PageResponse;
import com.example.shop.exception.BadRequestException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.AppUser;
import com.example.shop.model.Cart;
import com.example.shop.model.CartItem;
import com.example.shop.model.NotificationType;
import com.example.shop.model.OrderItem;
import com.example.shop.model.OrderStatus;
import com.example.shop.model.Product;
import com.example.shop.model.ShopOrder;
import com.example.shop.repository.CartRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orders;
    private final CartRepository carts;
    private final CurrentUser currentUser;
    private final PricingService pricing;
    private final NotificationService notifications;
    private final WhatsAppService whatsApp;

    public OrderResponse checkout(CheckoutRequest request) {
        AppUser user = currentUser.require();
        Cart cart = carts.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Your cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }

        ShopOrder order = new ShopOrder();
        order.setUser(user);
        order.setPaymentMethod(request.paymentMethod());
        order.setShippingName(request.shippingName().trim());
        order.setShippingPhone(request.shippingPhone().trim());
        order.setShippingAddress(request.shippingAddress().trim());
        order.setShippingCity(request.shippingCity().trim());
        order.setNotes(request.notes());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (!product.isActive() || cartItem.getQuantity() > product.getStock()) {
                throw new BadRequestException("Not enough stock for '" + product.getName() + "'");
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            order.getItems().add(orderItem);
            subtotal = subtotal.add(orderItem.getLineTotal());
        }

        BigDecimal shipping = pricing.shipping(subtotal);
        order.setSubtotal(subtotal);
        order.setShippingFee(shipping);
        order.setTotal(subtotal.add(shipping));
        orders.save(order);
        order.setOrderNumber(String.format("ORD-%06d", order.getId()));
        cart.getItems().clear();
        notifications.send(user, NotificationType.ORDER, "Order placed",
                "We received your order " + order.getOrderNumber() + ". Total: KES "
                        + order.getTotal());
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> myOrders(String query, int page, int size) {
        String like = "%" + (query == null ? "" : query.trim().toLowerCase()) + "%";
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ShopOrder> result = orders.search(currentUser.require(), like, pageable);
        return PageResponse.from(result.map(OrderResponse::from));
    }

    @Transactional(readOnly = true)
    public OrderResponse getMine(Long id) {
        return OrderResponse.from(findMine(id));
    }

    public OrderResponse cancel(Long id) {
        ShopOrder order = findMine(id);
        if (order.getStatus() != OrderStatus.PENDING
                && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("This order can no longer be cancelled");
        }
        restoreStock(order);
        order.setStatus(OrderStatus.CANCELED);
        notifications.send(order.getUser(), NotificationType.ORDER, "Order cancelled",
                "Your order " + order.getOrderNumber() + " was cancelled.");
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public String whatsappLink(Long id) {
        ShopOrder order = findMine(id);
        return whatsApp.shopLink("Hello, I need help with order " + order.getOrderNumber()
                + " (total KES " + order.getTotal() + ")");
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> adminList(OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ShopOrder> result = status == null
                ? orders.findAll(pageable)
                : orders.findByStatus(status, pageable);
        return PageResponse.from(result.map(OrderResponse::from));
    }

    public OrderResponse updateStatus(Long id, OrderStatus next) {
        ShopOrder order = orders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        OrderStatus current = order.getStatus();
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELED) {
            throw new BadRequestException("This order is already " + current);
        }
        if (next != OrderStatus.CANCELED && next.ordinal() <= current.ordinal()) {
            throw new BadRequestException("An order can only move forward: " + current
                    + " to " + next + " is not allowed");
        }
        if (next == OrderStatus.CANCELED) {
            restoreStock(order);
        }
        order.setStatus(next);
        notifications.send(order.getUser(), NotificationType.ORDER, "Order update",
                "Your order " + order.getOrderNumber() + " is now " + next + ".");
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public String customerWhatsappLink(Long id) {
        ShopOrder order = orders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        AppUser user = order.getUser();
        String number = user.getWhatsappNumber() != null
                ? user.getWhatsappNumber()
                : order.getShippingPhone();
        return whatsApp.link(number, "Hello " + order.getShippingName()
                + ", this is about your order " + order.getOrderNumber() + ".");
    }

    private ShopOrder findMine(Long id) {
        return orders.findByIdAndUser(id, currentUser.require())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private void restoreStock(ShopOrder order) {
        order.getItems().forEach(item -> item.getProduct().setStock(
                item.getProduct().getStock() + item.getQuantity()));
    }
}
