package com.shubham.flashsale.flashsale.order.controller;

import com.shubham.flashsale.flashsale.order.dto.OrderResponse;
import com.shubham.flashsale.flashsale.order.service.OrderService;
import com.shubham.flashsale.ratelimit.annotation.RateLimit;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

  private final OrderService orderService;

  @GetMapping
  @RateLimit(policy = RateLimitPolicy.ADMIN)
  public ResponseEntity<List<OrderResponse>> getAllOrders() {
    return ResponseEntity.ok(orderService.getAllOrders());
  }

  @GetMapping("/users/{userUuid}")
  @RateLimit(policy = RateLimitPolicy.ADMIN)
  public ResponseEntity<List<OrderResponse>> getOrdersByUser(@PathVariable UUID userUuid) {
    return ResponseEntity.ok(orderService.getOrdersByUser(userUuid.toString()));
  }
}
