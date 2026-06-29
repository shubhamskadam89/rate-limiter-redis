package com.shubham.flashsale.flashsale.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    UUID orderUuid;

    UUID saleItemUuid;

    UUID productUuid;

    String productName;

    Integer quantity;

    BigDecimal unitPrice;

    BigDecimal totalPrice;

    String status;

    Instant createdAt;

}