package com.shubham.flashsale.flashsale.order.controller;


import com.shubham.flashsale.flashsale.order.dto.OrderResponse;
import com.shubham.flashsale.flashsale.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{orderUuid}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable UUID orderUuid
    ) {
        return ResponseEntity.ok(
                orderService.getOrder(orderUuid.toString())
        );
    }
}