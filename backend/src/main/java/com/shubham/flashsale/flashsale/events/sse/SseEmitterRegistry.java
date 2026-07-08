package com.shubham.flashsale.flashsale.events.sse;

import com.shubham.flashsale.flashsale.events.StockUpdateEvent;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Slf4j
public class SseEmitterRegistry {

  private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> emitters =
      new ConcurrentHashMap<>();

  public SseEmitter register(String saleItemUuid) {
    SseEmitter emitter = new SseEmitter(0L);

    emitters.computeIfAbsent(saleItemUuid, key -> new CopyOnWriteArrayList<>()).add(emitter);

    emitter.onCompletion(() -> remove(saleItemUuid, emitter));
    emitter.onTimeout(() -> remove(saleItemUuid, emitter));
    emitter.onError(error -> remove(saleItemUuid, emitter));

    try {
      emitter.send(SseEmitter.event().name("connected").data("Subscribed successfully"));
    } catch (IOException e) {
      remove(saleItemUuid, emitter);
    }

    log.info("SSE client connected. saleItemUuid={}", saleItemUuid);

    return emitter;
  }

  public void broadcast(StockUpdateEvent event) {
    List<SseEmitter> saleItemEmitters = emitters.get(event.getSaleItemUuid());

    if (saleItemEmitters == null || saleItemEmitters.isEmpty()) {
      return;
    }

    for (SseEmitter emitter : saleItemEmitters) {
      try {
        emitter.send(SseEmitter.event().name("stock-update").data(event));
      } catch (IOException e) {
        remove(event.getSaleItemUuid(), emitter);
      }
    }
  }

  private void remove(String saleItemUuid, SseEmitter emitter) {
    List<SseEmitter> saleItemEmitters = emitters.get(saleItemUuid);

    if (saleItemEmitters != null) {
      saleItemEmitters.remove(emitter);

      if (saleItemEmitters.isEmpty()) {
        emitters.remove(saleItemUuid);
      }
    }

    log.info("SSE client disconnected. saleItemUuid={}", saleItemUuid);
  }

  @Scheduled(fixedRate = 30000)
  public void sendHeartbeat() {

    emitters.forEach(
        (saleItemUuid, emitterList) -> {
          for (SseEmitter emitter : emitterList) {

            try {
              emitter.send(SseEmitter.event().name("ping").data("keep-alive"));

            } catch (IOException e) {
              remove(saleItemUuid, emitter);
            }
          }
        });
  }
}
