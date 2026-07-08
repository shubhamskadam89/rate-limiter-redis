package com.shubham.flashsale.flashsale.events;

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
public class RedisStockUpdatePublisher implements StockUpdatePublisher {

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void publish(StockUpdateEvent event) {
    try {
      String channel = RedisKeyBuilder.stockUpdates(event.getSaleItemUuid());
      String payload = objectMapper.writeValueAsString(event);

      redisTemplate.convertAndSend(channel, payload);

      log.info(
          "Published stock update. channel={}, saleUuid={}, saleItemUuid={}, remainingInventory={}",
          channel,
          event.getSaleUuid(),
          event.getSaleItemUuid(),
          event.getRemainingInventory());
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to publish stock update event", e);
    }
  }
}
