package com.shubham.flashsale.flashsale.order.service.lua;

import com.shubham.flashsale.common.redis.RedisKeyBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashSalePurchaseExecutor {

    private final StringRedisTemplate redisTemplate;

    private final RedisScript<List> flashSalePurchaseScript;


    public PurchaseResult execute(
            Long saleItemId,
            Long userId,
            int quantity,
            int maxPerUser
    ){
        String inventoryKey = RedisKeyBuilder.inventory(saleItemId);

        String userPurchaseKey = RedisKeyBuilder.userPurchase(saleItemId,userId);

        List<?>result  = redisTemplate.execute(
                flashSalePurchaseScript,
                List.of(inventoryKey,userPurchaseKey),
                String.valueOf(quantity),
                String.valueOf(maxPerUser)
        );

        if(result==null || result.size()!=2 ){
            throw new IllegalStateException("Invalid lua script response");
        }
        long first = ((Number) result.get(0)).longValue();
        long second = ((Number) result.get(1)).longValue();

        if (first == -1) {
            return new PurchaseResult(
                    PurchaseStatus.SOLD_OUT,
                    0,
                    (int) second
            );
        }

        if (first == -2) {
            return new PurchaseResult(
                    PurchaseStatus.LIMIT_EXCEEDED,
                    0,
                    (int) second
            );
        }
        return new PurchaseResult(
                PurchaseStatus.SUCCESS,
                (int) first,
                (int) second
        );

    }


}
