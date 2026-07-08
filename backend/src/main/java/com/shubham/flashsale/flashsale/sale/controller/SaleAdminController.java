package com.shubham.flashsale.flashsale.sale.controller;

import com.shubham.flashsale.flashsale.sale.dto.AddSaleItemRequest;
import com.shubham.flashsale.flashsale.sale.dto.CreateSaleRequest;
import com.shubham.flashsale.flashsale.sale.dto.SaleDetailResponse;
import com.shubham.flashsale.flashsale.sale.dto.SaleItemResponse;
import com.shubham.flashsale.flashsale.sale.dto.SaleResponse;
import com.shubham.flashsale.flashsale.sale.service.SaleService;
import com.shubham.flashsale.ratelimit.annotation.RateLimit;
import com.shubham.flashsale.ratelimit.resolver.policy.RateLimitPolicy;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/sales")
@RequiredArgsConstructor
public class SaleAdminController {

  private final SaleService saleService;

  @GetMapping
  @RateLimit(policy = RateLimitPolicy.ADMIN)
  public ResponseEntity<List<SaleDetailResponse>> getSales() {
    return ResponseEntity.ok(saleService.getAdminSales());
  }

  @GetMapping("/{saleUuid}")
  @RateLimit(policy = RateLimitPolicy.ADMIN)
  public ResponseEntity<SaleDetailResponse> getSale(@PathVariable UUID saleUuid) {
    return ResponseEntity.ok(saleService.getSaleDetail(saleUuid.toString()));
  }

  @GetMapping("/{saleUuid}/items")
  @RateLimit(policy = RateLimitPolicy.ADMIN)
  public ResponseEntity<List<SaleItemResponse>> getSaleItems(@PathVariable UUID saleUuid) {
    return ResponseEntity.ok(saleService.getSaleItems(saleUuid.toString()));
  }

  @PostMapping
  @RateLimit(policy = RateLimitPolicy.ADMIN)
  public ResponseEntity<SaleResponse> createSale(@RequestBody CreateSaleRequest request) {
    return ResponseEntity.ok(saleService.createSale(request));
  }

  @PostMapping("/{saleUuid}/activate")
  @RateLimit(policy = RateLimitPolicy.ADMIN)
  public ResponseEntity<SaleResponse> activateSale(@PathVariable UUID saleUuid) {
    return ResponseEntity.ok(saleService.activateSale(saleUuid.toString()));
  }

  @PostMapping("/{saleUuid}/items")
  @RateLimit(policy = RateLimitPolicy.ADMIN)
  public ResponseEntity<SaleItemResponse> addItemToSale(
      @PathVariable UUID saleUuid, @RequestBody AddSaleItemRequest request) {
    return ResponseEntity.ok(saleService.addItemToSale(saleUuid.toString(), request));
  }
}
