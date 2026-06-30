package com.shubham.flashsale.flashsale.order.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseResponse {

    UUID orderUuid;

    UUID saleItemUuid;

    UUID productUuid;

    private Integer quantity;

    private Integer remainingInventory;

    private String message;

}