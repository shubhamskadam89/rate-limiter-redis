package com.shubham.flashsale.flashsale.sale.controller;


import com.shubham.flashsale.flashsale.sale.dto.AddSaleItemRequest;
import com.shubham.flashsale.flashsale.sale.dto.CreateSaleRequest;
import com.shubham.flashsale.flashsale.sale.dto.SaleItemResponse;
import com.shubham.flashsale.flashsale.sale.dto.SaleResponse;
import com.shubham.flashsale.flashsale.sale.service.SaleService;
import com.shubham.flashsale.flashsale.sale.service.SaleServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/admin/sales")
@RequiredArgsConstructor
public class SaleAdminController {

    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<SaleResponse> createSale(
            @RequestBody CreateSaleRequest request
    ) {
        return ResponseEntity.ok(
                saleService.createSale(request)
        );
    }

    @PostMapping("/{saleUuid}/activate")
    public ResponseEntity<SaleResponse> activateSale(
            @PathVariable UUID saleUuid
    ) {
        return ResponseEntity.ok(
                saleService.activateSale(saleUuid.toString())
        );
    }

    @PostMapping("/{saleUuid}/items")
    public ResponseEntity<SaleItemResponse> addItemToSale(
            @PathVariable UUID saleUuid,
            @RequestBody AddSaleItemRequest request
    ) {
        return ResponseEntity.ok(
                saleService.addItemToSale(saleUuid.toString(), request)
        );
    }
}