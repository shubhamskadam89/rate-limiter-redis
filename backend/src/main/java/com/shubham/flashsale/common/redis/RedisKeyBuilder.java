package com.shubham.flashsale.common.redis;

import java.util.UUID;

public final class RedisKeyBuilder {

    private RedisKeyBuilder() {}

    // ===========================
    // Rate Limiting
    // ===========================

    public static String fixedWindow(String identifier) {
        return "rate:fw:" + identifier;
    }

    public static String slidingWindow(String identifier) {
        return "rate:sw:" + identifier;
    }

    public static String tokenBucket(String identifier) {
        return "rate:tb:" + identifier;
    }

    // ===========================
    // Flash Sale
    // ===========================

    public static String inventory(String saleItemUuid) {
        return "inventory:" + saleItemUuid;
    }

    public static String userPurchase(String saleItemUuid, String userUuid) {
        return "user_purchases:" + saleItemUuid + ":" + userUuid;
    }

    // ===========================
    // Idempotency
    // ===========================

    public static String idempotency(String key) {
        return "idem:" + key;
    }

    // ===========================
    // Async Order Queue
    // ===========================

    public static String orderQueue() {
        return "orders:queue";
    }

    // ===========================
    // Pub/Sub
    // ===========================

    public static String stockUpdates(String saleItemUuid) {
        return "stock:updates:" + saleItemUuid;
    }

    public static String orderRetryQueue() {
        return "orders:queue:retry";
    }
}