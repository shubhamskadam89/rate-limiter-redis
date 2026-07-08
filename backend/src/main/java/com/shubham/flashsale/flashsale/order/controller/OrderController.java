package com.shubham.flashsale.flashsale.order.controller;

import com.shubham.flashsale.flashsale.order.dto.OrderResponse;
import com.shubham.flashsale.flashsale.order.service.OrderService;
import com.shubham.flashsale.ratelimit.annotation.RateLimit;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @GetMapping
  @RateLimit(policy = RateLimitPolicy.GENERAL)
  public ResponseEntity<List<OrderResponse>> getMyOrders() {
    return ResponseEntity.ok(orderService.getCurrentUserOrders());
  }

  @GetMapping("/{orderUuid}")
  @RateLimit(policy = RateLimitPolicy.GENERAL)
  public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderUuid) {
    return ResponseEntity.ok(orderService.getOrder(orderUuid.toString()));
  }
}
