package com.shubham.flashsale.flashsale.sale.repository;

import com.shubham.flashsale.flashsale.sale.entity.SaleEvent;
import com.shubham.flashsale.flashsale.sale.entity.SaleItem;
import com.shubham.flashsale.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SaleItemRepository
        extends JpaRepository<SaleItem, Long> {

    List<SaleItem> findBySaleEvent(SaleEvent saleEvent);

    boolean existsBySaleEventAndProduct(
            SaleEvent saleEvent,
            Product product
    );
    Optional<SaleItem> findByUuid(String uuid);
}