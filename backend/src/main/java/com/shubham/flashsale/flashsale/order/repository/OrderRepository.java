package com.shubham.flashsale.flashsale.order.repository;

import com.shubham.flashsale.flashsale.order.entity.Order;
import com.shubham.flashsale.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
  Optional<Order> findByUuid(String uuid);

  Optional<Order> findByIdempotencyKey(String key);

  List<Order> findAllByOrderByCreatedAtDesc();

  List<Order> findByUserOrderByCreatedAtDesc(User user);
}
