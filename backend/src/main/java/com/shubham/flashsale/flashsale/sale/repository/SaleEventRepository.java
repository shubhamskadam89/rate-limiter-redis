package com.shubham.flashsale.flashsale.sale.repository;

import com.shubham.flashsale.flashsale.sale.entity.SaleEvent;
import com.shubham.flashsale.flashsale.sale.entity.Status;
import com.shubham.flashsale.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleEventRepository extends JpaRepository<SaleEvent, Long> {

  List<SaleEvent> findByStatus(Status status);

  List<SaleEvent> findByStatusAndStartTimeBefore(Status status, LocalDateTime now);

  List<SaleEvent> findByStatusAndEndTimeBefore(Status status, LocalDateTime now);

  Optional<SaleEvent> findByUuid(String uuid);

  List<SaleEvent> findByCreatedByOrderByCreatedAtDesc(User createdBy);

  List<SaleEvent> findAllByOrderByCreatedAtDesc();
}
