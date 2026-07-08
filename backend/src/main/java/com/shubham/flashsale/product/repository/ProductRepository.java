package com.shubham.flashsale.product.repository;

import com.shubham.flashsale.product.entity.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
  Optional<Product> findByUuid(String uuid);

  boolean existsByUuid(String uuid);
}
