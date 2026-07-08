package com.shubham.flashsale.flashsale.order.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shubham.flashsale.common.redis.RedisKeyBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderQueueProducer {

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public void enqueue(OrderQueueMessage msg) {
    try {
      String payload = objectMapper.writeValueAsString(msg);

      redisTemplate.opsForList().leftPush(RedisKeyBuilder.orderQueue(), payload);
      log.info(
          "Order message queued. orderUuid={}, userUuid={}, saleItemUuid={}, queueKey={}",
          msg.getOrderUuid(),
          msg.getUserUuid(),
          msg.getSaleItemUuid(),
          RedisKeyBuilder.orderQueue());
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize order queue message. orderUuid={}", msg.getOrderUuid(), e);
      throw new IllegalStateException("Failed to enqueue order message", e);
    }
  }
}
