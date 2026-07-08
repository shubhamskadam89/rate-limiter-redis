package com.shubham.flashsale.flashsale.events.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shubham.flashsale.flashsale.events.StockUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StockUpdateSubscriber {

  private final ObjectMapper objectMapper;
  private final SseEmitterRegistry emitterRegistry;

  public void handleMessage(String message) {
    try {
      StockUpdateEvent event = objectMapper.readValue(message, StockUpdateEvent.class);

      log.info(
          "Received stock update event from Redis. saleItemUuid={}, remainingInventory={}",
          event.getSaleItemUuid(),
          event.getRemainingInventory());

      emitterRegistry.broadcast(event);

    } catch (JsonProcessingException e) {
      log.error("Failed to deserialize stock update event. payload={}", message, e);
    }
  }
}
