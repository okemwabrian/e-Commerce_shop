package com.example.shop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {
    @Value("${shop.shipping-fee}")
    private BigDecimal shippingFee;

    @Value("${shop.free-shipping-threshold}")
    private BigDecimal freeThreshold;

    public BigDecimal shipping(BigDecimal subtotal) {
        if (subtotal.signum() == 0 || subtotal.compareTo(freeThreshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return shippingFee;
    }
}
