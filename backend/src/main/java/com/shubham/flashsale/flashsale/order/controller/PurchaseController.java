package com.shubham.flashsale.flashsale.order.controller;

import com.shubham.flashsale.flashsale.order.dto.PurchaseRequest;
import com.shubham.flashsale.flashsale.order.dto.PurchaseResponse;
import com.shubham.flashsale.flashsale.order.service.PurchaseService;
import com.shubham.flashsale.ratelimit.annotation.RateLimit;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class PurchaseController {

  private final PurchaseService purchaseService;

  @PostMapping("/{saleUuid}/items/{saleItemUuid}/purchase")
  @RateLimit(policy = RateLimitPolicy.TRANSACTION)
  public ResponseEntity<PurchaseResponse> purchase(
      @PathVariable UUID saleUuid,
      @PathVariable UUID saleItemUuid,
      @RequestHeader("X-Idempotency-Key") String idempotencyKey,
      @Valid @RequestBody PurchaseRequest request) {
    return ResponseEntity.ok(
        purchaseService.purchase(
            saleUuid.toString(), saleItemUuid.toString(), idempotencyKey, request));
  }
}
