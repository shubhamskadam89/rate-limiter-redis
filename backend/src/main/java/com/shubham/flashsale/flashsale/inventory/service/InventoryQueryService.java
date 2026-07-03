package com.shubham.flashsale.flashsale.inventory.service;

import com.shubham.flashsale.common.redis.RedisKeyBuilder;
import com.shubham.flashsale.exception.inventory.InventoryStateUnavailableException;
import com.shubham.flashsale.exception.sale.SaleItemNotFoundException;
import com.shubham.flashsale.flashsale.inventory.dto.InventoryResponse;
import com.shubham.flashsale.flashsale.sale.entity.SaleEvent;
import com.shubham.flashsale.flashsale.sale.entity.SaleItem;
import com.shubham.flashsale.flashsale.sale.entity.Status;
import com.shubham.flashsale.flashsale.sale.repository.SaleItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryQueryService {

    private final SaleItemRepository saleItemRepository;
    private final StringRedisTemplate redisTemplate;

    public InventoryResponse getInventory(String saleItemUuid) {
        log.debug("Querying inventory for saleItemUuid={}", saleItemUuid);
        SaleItem saleItem = saleItemRepository.findByUuid(saleItemUuid)
                .orElseThrow(() -> {
                    log.warn("Sale item not found while querying inventory: {}", saleItemUuid);
                    return new SaleItemNotFoundException(saleItemUuid);
                });
        SaleEvent saleEvent = saleItem.getSaleEvent();
        Instant asOf = Instant.now();

        if (saleEvent.getStatus() == Status.ENDED || saleEvent.getEndTime().isBefore(LocalDateTime.now())) {
            log.debug("Sale ended for saleItemUuid={}", saleItemUuid);
            return InventoryResponse.builder()
                    .saleItemUuid(UUID.fromString(saleItem.getUuid()))
                    .remainingInventory(null)
                    .availability("SALE_ENDED")
                    .asOf(asOf)
                    .source("UNAVAILABLE")
                    .build();
        }

        if (saleEvent.getStatus() != Status.ACTIVE) {
            log.debug("Sale not active for saleItemUuid={}", saleItemUuid);
            return InventoryResponse.builder()
                    .saleItemUuid(UUID.fromString(saleItem.getUuid()))
                    .remainingInventory(saleItem.getInventory())
                    .availability("NOT_ACTIVE")
                    .asOf(asOf)
                    .source("DATABASE_INITIAL")
                    .build();
        }

        String redisValue = redisTemplate.opsForValue()
                .get(RedisKeyBuilder.inventory(saleItem.getUuid()));

        if (redisValue != null) {
            long remainingInventory = Long.parseLong(redisValue);
            log.debug("Inventory fetched from Redis for saleItemUuid={}, remaining={}", saleItemUuid, remainingInventory);
            return InventoryResponse.builder()
                    .saleItemUuid(UUID.fromString(saleItem.getUuid()))
                    .remainingInventory(remainingInventory)
                    .availability(remainingInventory <= 0 ? "SOLD_OUT" : "AVAILABLE")
                    .asOf(asOf)
                    .source("REDIS")
                    .build();
        }

        log.error("Inventory state unavailable in Redis and DB for saleItemUuid={}", saleItemUuid);
        throw new InventoryStateUnavailableException(saleItem.getUuid());
    }
}
