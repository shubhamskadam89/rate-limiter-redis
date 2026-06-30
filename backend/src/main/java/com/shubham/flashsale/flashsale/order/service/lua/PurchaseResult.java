package com.shubham.flashsale.flashsale.order.service.lua;

import lombok.AllArgsConstructor;
import lombok.Getter;

public record PurchaseResult(
        PurchaseStatus status,
        int remainingInventory,
        int userPurchaseCount
) {}


