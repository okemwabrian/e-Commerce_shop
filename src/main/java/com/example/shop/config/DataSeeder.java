package com.example.shop.config;

import com.example.shop.model.AppUser;
import com.example.shop.model.Category;
import com.example.shop.model.Product;
import com.example.shop.model.Role;
import com.example.shop.repository.AppUserRepository;
import com.example.shop.repository.CategoryRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "shop.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {
    private final AppUserRepository users;
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();
        Map<String, List<String>> categoryTree = categoryTree();
        seedCategories(categoryTree);
        seedProducts();
    }

    private void seedAdmin() {
        String email = adminEmail.trim().toLowerCase();
        if (users.existsByEmail(email)) {
            return;
        }
        AppUser admin = new AppUser();
        admin.setFullName("Shop Admin");
        admin.setEmail(email);
        admin.setPassword(encoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        users.save(admin);
    }

    private Map<String, List<String>> categoryTree() {
        Map<String, List<String>> data = new LinkedHashMap<>();
        data.put("Phones and Tablets", List.of("Smartphones", "Tablets", "Phone Accessories"));
        data.put("TVs and Radios", List.of("Televisions", "Home Theatre", "Radios"));
        data.put("Appliances", List.of("Kitchen Appliances", "Refrigerators and Freezers",
                "Washing Machines"));
        data.put("Health and Beauty", List.of("Skincare", "Hair Care", "Fragrances",
                "Personal Care"));
        data.put("Home and Office", List.of("Furniture", "Office Supplies", "Home Decor"));
        data.put("Fashion", List.of("Mens Fashion", "Womens Fashion", "Shoes", "Bags"));
        data.put("Computing", List.of("Laptops", "Desktops", "Printers and Scanners",
                "Computer Accessories"));
        data.put("Gaming", List.of("Consoles", "Video Games", "Gaming Accessories"));
        data.put("Baby Products", List.of("Diapers and Wipes", "Feeding", "Baby Toys",
                "Baby Clothing"));
        return data;
    }

    private void seedCategories(Map<String, List<String>> data) {
        data.forEach((parentName, children) -> {
            Category parent = saveCategory(parentName, null);
            children.forEach(childName -> saveCategory(childName, parent));
        });
    }

    private Category saveCategory(String name, Category parent) {
        String slug = CategoryService.slugify(name);
        return categories.findBySlug(slug).orElseGet(() -> {
            Category category = new Category();
            category.setName(name);
            category.setSlug(slug);
            category.setParent(parent);
            return categories.save(category);
        });
    }

    private void seedProducts() {
        if (products.count() > 0) {
            return;
        }
        product("Galaxy Phone 128GB", "Samsung", "24999", 20, "smartphones",
                "6.5 inch AMOLED, 50MP camera");
        product("Budget Smartphone 64GB", "Tecno", "11999", 35, "smartphones",
                "Big battery, dual SIM");
        product("10 inch Tablet", "Lenovo", "18500", 12, "tablets",
                "Ideal for study and movies");
        product("43 inch Smart TV", "Hisense", "32999", 8, "televisions",
                "4K UHD, built-in streaming apps");
        product("Gaming Laptop 16GB", "HP", "89999", 5, "laptops",
                "RTX graphics, 512GB SSD");
        product("Wireless Mouse", "Logitech", "1500", 60, "computer-accessories",
                "2.4GHz optical mouse");
        product("Baby Diapers Pack", "Pampers", "1800", 100, "diapers-and-wipes",
                "Size 3, 60 pieces");
        product("Running Shoes", "Nike", "6500", 25, "shoes",
                "Lightweight daily trainers");
    }

    private void product(String name, String brand, String price, int stock, String slug,
                         String description) {
        Product product = new Product();
        product.setName(name);
        product.setBrand(brand);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        product.setDescription(description);
        product.setImageUrl("https://placehold.co/600x400?text=" + name.replace(" ", "+"));
        product.setCategory(categories.findBySlug(slug).orElseThrow());
        products.save(product);
    }
}
