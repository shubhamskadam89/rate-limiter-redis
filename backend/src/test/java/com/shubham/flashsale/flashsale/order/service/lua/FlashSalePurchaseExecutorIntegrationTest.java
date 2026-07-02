package com.shubham.flashsale.flashsale.order.service.lua;

import com.shubham.flashsale.common.redis.RedisKeyBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class FlashSalePurchaseExecutorIntegrationTest {

    private static final int INVENTORY = 100;
    private static final int THREADS = 500;
    private static final int QUANTITY = 1;
    private static final int MAX_PER_USER = 1;

    @Autowired
    private FlashSalePurchaseExecutor purchaseExecutor;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String saleItemUuid;
    private Set<String> redisKeysToClean;

    @BeforeEach
    void setUp() {
        saleItemUuid = UUID.randomUUID().toString();
        redisKeysToClean = ConcurrentHashMap.newKeySet();

        String inventoryKey = RedisKeyBuilder.inventory(saleItemUuid);
        redisKeysToClean.add(inventoryKey);

        redisTemplate.delete(inventoryKey);
        redisTemplate.opsForValue().set(inventoryKey, String.valueOf(INVENTORY));
    }

    @AfterEach
    void cleanUp() {
        if (redisKeysToClean != null && !redisKeysToClean.isEmpty()) {
            redisTemplate.delete(redisKeysToClean);
        }
    }

    @Test
    void shouldReserveExactlyAvailableInventoryWithoutOversellingUnderConcurrentLoad()
            throws InterruptedException {

        CountDownLatch readyGate = new CountDownLatch(THREADS);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(THREADS);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger soldOutCount = new AtomicInteger();
        AtomicInteger limitExceededCount = new AtomicInteger();
        AtomicInteger inventoryNotLoadedCount = new AtomicInteger();

        ConcurrentLinkedQueue<Throwable> unexpectedErrors = new ConcurrentLinkedQueue<>();

        try (ExecutorService executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {

            for (int i = 0; i < THREADS; i++) {
                String userUuid = UUID.randomUUID().toString();
                String userPurchaseKey = RedisKeyBuilder.userPurchase(saleItemUuid, userUuid);
                redisKeysToClean.add(userPurchaseKey);

                executor.submit(() -> {
                    try {
                        readyGate.countDown();
                        startGate.await();

                        PurchaseResult result = purchaseExecutor.execute(
                                saleItemUuid,
                                userUuid,
                                QUANTITY,
                                MAX_PER_USER
                        );

                        switch (result.status()) {
                            case SUCCESS -> successCount.incrementAndGet();
                            case SOLD_OUT -> soldOutCount.incrementAndGet();
                            case LIMIT_EXCEEDED -> limitExceededCount.incrementAndGet();
                            case INVENTORY_NOT_LOADED -> inventoryNotLoadedCount.incrementAndGet();
                        };
                    } catch (Throwable throwable) {
                        unexpectedErrors.add(throwable);
                    } finally {
                        endGate.countDown();
                    }
                });
            }

            assertTrue(
                    readyGate.await(10, TimeUnit.SECONDS),
                    "All purchase tasks should be ready before the test starts"
            );

            startGate.countDown();

            assertTrue(
                    endGate.await(30, TimeUnit.SECONDS),
                    "All concurrent purchase attempts should finish within 30 seconds"
            );
        }

        String inventoryKey = RedisKeyBuilder.inventory(saleItemUuid);
        String remainingInventory = redisTemplate.opsForValue().get(inventoryKey);

        assertTrue(
                unexpectedErrors.isEmpty(),
                () -> "Unexpected errors occurred: " + unexpectedErrors
        );

        assertEquals(
                INVENTORY,
                successCount.get(),
                "Exactly available inventory units must be reserved"
        );

        assertEquals(
                THREADS - INVENTORY,
                soldOutCount.get(),
                "Every request after inventory is exhausted must be sold out"
        );

        assertEquals(
                0,
                limitExceededCount.get(),
                "Distinct users must not trigger the per-user purchase limit"
        );

        assertEquals(
                0,
                inventoryNotLoadedCount.get(),
                "Inventory is explicitly loaded before concurrent purchase attempts"
        );

        assertEquals(
                "0",
                remainingInventory,
                "Redis inventory must end at exactly zero and never become negative"
        );
    }
}
