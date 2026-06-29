package com.shubham.flashsale.flashsale.order.controller;

import com.shubham.flashsale.exception.sale.SaleNotActiveException;
import com.shubham.flashsale.flashsale.order.dto.PurchaseRequest;
import com.shubham.flashsale.flashsale.order.dto.PurchaseResponse;
import com.shubham.flashsale.flashsale.order.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping("/{saleUuid}/items/{saleItemUuid}/purchase")
    public ResponseEntity<PurchaseResponse> purchase(
            @PathVariable UUID saleUuid,
            @PathVariable UUID saleItemUuid,
            @Valid @RequestBody PurchaseRequest request
    ) throws SaleNotActiveException {

        return ResponseEntity.ok(
                purchaseService.purchase(
                        saleUuid.toString(),
                        saleItemUuid.toString(),
                        request
                )
        );
    }
}