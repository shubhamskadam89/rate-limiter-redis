package com.shubham.flashsale.flashsale.events.sse;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/stock-updates")
@RequiredArgsConstructor
public class StockUpdateController {

  private final SseEmitterRegistry emitterRegistry;

  @GetMapping(value = "/{saleItemUuid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamStockUpdates(@PathVariable UUID saleItemUuid) {
    return emitterRegistry.register(saleItemUuid.toString());
  }
}
