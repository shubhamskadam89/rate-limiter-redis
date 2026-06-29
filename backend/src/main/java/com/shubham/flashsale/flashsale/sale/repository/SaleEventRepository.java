package com.shubham.flashsale.flashsale.sale.repository;


import com.shubham.flashsale.flashsale.sale.entity.SaleEvent;
import com.shubham.flashsale.flashsale.sale.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleEventRepository extends JpaRepository<SaleEvent,Long> {

    List<SaleEvent> findByStatus(Status status);

    List<SaleEvent> findByStatusAndStartTimeBefore(
            Status status,
            LocalDateTime now
    );

    List<SaleEvent> findByStatusAndEndTimeBefore(
            Status status,
            LocalDateTime now
    );
    Optional<SaleEvent> findByUuid(String uuid);

}