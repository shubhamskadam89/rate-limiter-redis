package com.shubham.flashsale.flashsale.order.service.lua;

import com.shubham.flashsale.common.redis.RedisKeyBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlashSalePurchaseExecutor {

    private final StringRedisTemplate redisTemplate;

    private final RedisScript<List> flashSalePurchaseScript;


    public PurchaseResult execute(
            String saleItemUuid,
            String userUuid,
            int quantity,
            int maxPerUser
    ) {
        log.debug("Executing Lua purchase script for saleItemUuid={}, userUuid={}, quantity={}", saleItemUuid, userUuid, quantity);
        String inventoryKey = RedisKeyBuilder.inventory(saleItemUuid);
        String userPurchaseKey = RedisKeyBuilder.userPurchase(saleItemUuid, userUuid);

        List<?> result = redisTemplate.execute(
                flashSalePurchaseScript,
                List.of(inventoryKey, userPurchaseKey),
                String.valueOf(quantity),
                String.valueOf(maxPerUser)
        );

        if (result == null || result.size() != 2) {
            log.error("Invalid lua script response for saleItemUuid={}", saleItemUuid);
            throw new IllegalStateException("Invalid lua script response");
        }

        long first = ((Number) result.get(0)).longValue();
        long second = ((Number) result.get(1)).longValue();

        if (first == -1) {
            log.warn("Purchase failed: SOLD_OUT for saleItemUuid={}, userUuid={}", saleItemUuid, userUuid);
            return new PurchaseResult(PurchaseStatus.SOLD_OUT, 0, (int) second);
        }

        if (first == -2) {
            log.warn("Purchase failed: LIMIT_EXCEEDED for saleItemUuid={}, userUuid={}", saleItemUuid, userUuid);
            return new PurchaseResult(PurchaseStatus.LIMIT_EXCEEDED, 0, (int) second);
        }

        if (first == -3) {
            log.error("Purchase failed: INVENTORY_NOT_LOADED in Redis for saleItemUuid={}", saleItemUuid);
            return new PurchaseResult(PurchaseStatus.INVENTORY_NOT_LOADED, 0, 0);
        }

        log.debug("Lua purchase script execution SUCCESS for saleItemUuid={}, userUuid={}, remaining={}", saleItemUuid, userUuid, first);
        return new PurchaseResult(PurchaseStatus.SUCCESS, (int) first, (int) second);
    }


}
