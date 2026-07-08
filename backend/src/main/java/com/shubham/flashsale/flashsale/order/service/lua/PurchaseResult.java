package com.shubham.flashsale.flashsale.order.service.lua;

public record PurchaseResult(
    PurchaseStatus status, int remainingInventory, int userPurchaseCount) {}
