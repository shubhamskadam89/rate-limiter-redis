package com.shubham.flashsale.flashsale.inventory.controller;

import com.shubham.flashsale.flashsale.inventory.dto.InventoryResponse;
import com.shubham.flashsale.flashsale.inventory.service.InventoryQueryService;
import com.shubham.flashsale.ratelimit.annotation.RateLimit;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sale-items")
@RequiredArgsConstructor
public class InventoryController {

  private final InventoryQueryService inventoryQueryService;

  @GetMapping("/{saleItemUuid}/inventory")
  @RateLimit(policy = RateLimitPolicy.GENERAL)
  public ResponseEntity<InventoryResponse> getInventory(@PathVariable UUID saleItemUuid) {
    return ResponseEntity.ok(inventoryQueryService.getInventory(saleItemUuid.toString()));
  }
}
