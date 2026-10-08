package com.example.shop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {
    private PricingService pricing;

    @BeforeEach
    void setUp() {
        pricing = new PricingService();
        ReflectionTestUtils.setField(pricing, "shippingFee", new BigDecimal("300"));
        ReflectionTestUtils.setField(pricing, "freeThreshold", new BigDecimal("5000"));
    }

    @Test
    void emptyCartHasNoShipping() {
        assertEquals(BigDecimal.ZERO, pricing.shipping(BigDecimal.ZERO));
    }

    @Test
    void smallOrderPaysShipping() {
        assertEquals(new BigDecimal("300"), pricing.shipping(new BigDecimal("1500")));
    }

    @Test
    void orderAtThresholdShipsFree() {
        assertEquals(BigDecimal.ZERO, pricing.shipping(new BigDecimal("5000")));
    }
}
