package com.shubham.flashsale.flashsale.order.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shubham.flashsale.common.redis.RedisKeyBuilder;
import com.shubham.flashsale.flashsale.order.service.OrderPersistenceService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderPersistenceWorker {

    private final RedisConnectionFactory redisConnectionFactory;
    private final ObjectMapper objectMapper;
    private final OrderPersistenceService orderPersistenceService;

    private volatile boolean running = true;
    private Thread workerThread;

    @PostConstruct
    public void start() {
        workerThread = Thread.ofVirtual()
                .name("order-persistence-worker")
                .start(this::consume);
    }

    @PreDestroy
    public void stop() {
        running = false;

        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    private void consume() {
        log.info("Order persistence worker started. queueKey={}", RedisKeyBuilder.orderQueue());

        while (running) {
            String payload = null;

            try {
                List<byte[]> result = redisConnectionFactory
                        .getConnection()
                        .bRPop(
                                (int) Duration.ofSeconds(5).toSeconds(),
                                RedisKeyBuilder.orderQueue().getBytes(StandardCharsets.UTF_8)
                        );

                if (result == null || result.size() < 2) {
                    continue;
                }

                payload = new String(result.get(1), StandardCharsets.UTF_8);
                OrderQueueMessage message = deserialize(payload);

                orderPersistenceService.persist(message);

            } catch (Exception e) {
                log.error("Order persistence worker failed while consuming queue", e);

                if (payload != null) {
                    redisConnectionFactory.getConnection().lPush(
                            RedisKeyBuilder.orderRetryQueue().getBytes(StandardCharsets.UTF_8),
                            payload.getBytes(StandardCharsets.UTF_8)
                    );

                    log.warn(
                            "Order message moved to retry queue. retryQueueKey={}",
                            RedisKeyBuilder.orderRetryQueue()
                    );
                }
            }
        }

        log.info("Order persistence worker stopped");
    }

    private OrderQueueMessage deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, OrderQueueMessage.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize order queue message", e);
        }
    }
}