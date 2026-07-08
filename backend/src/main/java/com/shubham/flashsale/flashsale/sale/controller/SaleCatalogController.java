package com.shubham.flashsale.flashsale.sale.controller;

import com.shubham.flashsale.flashsale.sale.dto.SaleDetailResponse;
import com.shubham.flashsale.flashsale.sale.dto.SaleItemResponse;
import com.shubham.flashsale.flashsale.sale.service.SaleService;
import com.shubham.flashsale.ratelimit.annotation.RateLimit;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleCatalogController {

  private final SaleService saleService;

  @GetMapping
  @RateLimit(policy = RateLimitPolicy.GENERAL)
  public ResponseEntity<List<SaleDetailResponse>> getAvailableSales() {
    return ResponseEntity.ok(saleService.getAvailableSales());
  }

  @GetMapping("/{saleUuid}")
  @RateLimit(policy = RateLimitPolicy.GENERAL)
  public ResponseEntity<SaleDetailResponse> getSale(@PathVariable UUID saleUuid) {
    return ResponseEntity.ok(saleService.getSaleDetail(saleUuid.toString()));
  }

  @GetMapping("/{saleUuid}/items")
  @RateLimit(policy = RateLimitPolicy.GENERAL)
  public ResponseEntity<List<SaleItemResponse>> getSaleItems(@PathVariable UUID saleUuid) {
    return ResponseEntity.ok(saleService.getSaleItems(saleUuid.toString()));
  }
}
