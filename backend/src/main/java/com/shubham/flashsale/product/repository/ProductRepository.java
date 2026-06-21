package com.shubham.flashsale.product.repository;

import com.shubham.flashsale.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {


    Optional<Product> findByUuid(String uuid);




}