package com.shubham.flashsale.common.redis;

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

    public static String inventory(Long saleItemId) {
        return "inventory:" + saleItemId;
    }

    public static String userPurchase(Long saleItemId, Long userId) {
        return "user_purchases:" + saleItemId + ":" + userId;
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

    public static String stockUpdates(Long saleItemId) {
        return "stock:updates:" + saleItemId;
    }
}