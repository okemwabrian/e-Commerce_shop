package com.example.shop;

import com.example.shop.model.Category;
import com.example.shop.model.Product;
import com.example.shop.repository.CategoryRepository;
import com.example.shop.repository.ProductRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShopFlowIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductRepository products;

    @Autowired
    private CategoryRepository categories;

    private Long productId;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setName("Test");
        category.setSlug("test-" + UUID.randomUUID());
        category = categories.save(category);

        Product product = new Product();
        product.setName("Test Phone");
        product.setPrice(new BigDecimal("10000.00"));
        product.setStock(5);
        product.setCategory(category);
        productId = products.save(product).getId();
    }

    private String registerAndGetToken() throws Exception {
        String body = """
                {"fullName":"Test User","email":"%s@test.com","password":"Password123"}
                """.formatted(UUID.randomUUID());
        MvcResult result = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    @Test
    void cartRequiresLogin() throws Exception {
        mvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotCreateProducts() throws Exception {
        String token = registerAndGetToken();
        mvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void fullShoppingFlow() throws Exception {
        String authorization = "Bearer " + registerAndGetToken();
        mvc.perform(post("/api/cart/items")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.subtotal").value(20000.0));

        String checkout = """
                {"shippingName":"Test","shippingPhone":"0712345678",
                "shippingAddress":"Street 1","shippingCity":"Nairobi",
                "paymentMethod":"CASH_ON_DELIVERY"}
                """;
        mvc.perform(post("/api/orders")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkout))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").value(20000.0));

        assertEquals(3, products.findById(productId).orElseThrow().getStock());
        mvc.perform(get("/api/cart").header("Authorization", authorization))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void cannotBuyMoreThanStock() throws Exception {
        String authorization = "Bearer " + registerAndGetToken();
        mvc.perform(post("/api/cart/items")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":6}"))
                .andExpect(status().isBadRequest());
    }
}
