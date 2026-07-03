package com.shubham.flashsale.config;

import com.shubham.flashsale.product.entity.Product;
import com.shubham.flashsale.product.repository.ProductRepository;
import com.shubham.flashsale.user.entity.User;
import com.shubham.flashsale.user.entity.UserRole;
import com.shubham.flashsale.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        log.info("Checking if database seeding is required");

        if (userRepository.count() == 0) {
            log.info("Seeding default users (ADMIN, VIP, USER)");

            User admin = new User();
            admin.setEmail("admin@flashsale.com");
            admin.setFullName("Admin User");
            admin.setPasswordHash("$2a$10$dummyHash");
            admin.setRole(UserRole.ADMIN);

            User vip = new User();
            vip.setEmail("vip@flashsale.com");
            vip.setFullName("VIP User");
            vip.setPasswordHash("$2a$10$dummyHash");
            vip.setRole(UserRole.VIP);

            User user = new User();
            user.setEmail("user@flashsale.com");
            user.setFullName("Regular User");
            user.setPasswordHash("$2a$10$dummyHash");
            user.setRole(UserRole.USER);

            userRepository.save(admin);
            userRepository.save(vip);
            userRepository.save(user);
            log.info("Default users seeded successfully");
        }

        if (productRepository.count() == 0) {
            log.info("Seeding default products (Gaming Mouse, Mechanical Keyboard)");

            Product mouse = new Product();
            mouse.setName("Gaming Mouse");
            mouse.setDescription("RGB Wireless Gaming Mouse");
            mouse.setBasePrice(BigDecimal.valueOf(1999.00));
            mouse.setMetadata(Map.of(
                    "brand", "Logitech",
                    "color", "Black"
            ));

            Product keyboard = new Product();
            keyboard.setName("Mechanical Keyboard");
            keyboard.setDescription("Hot-swappable Mechanical Keyboard");
            keyboard.setBasePrice(BigDecimal.valueOf(4999.00));
            keyboard.setMetadata(Map.of(
                    "brand", "Keychron",
                    "switch", "Brown"
            ));

            productRepository.save(mouse);
            productRepository.save(keyboard);
            log.info("Default products seeded successfully");
        }
        log.info("Database seeding check completed");
    }
}