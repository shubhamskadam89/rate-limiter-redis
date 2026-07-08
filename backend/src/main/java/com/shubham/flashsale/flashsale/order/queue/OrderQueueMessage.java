package com.shubham.flashsale.flashsale.order.queue;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderQueueMessage {

  private String orderUuid;

  private String userUuid;

  private String saleItemUuid;

  private Integer quantity;

  private BigDecimal unitPrice;

  private BigDecimal totalPrice;

  private String idempotencyKey;

  public static OrderQueueMessage create(
      String userUuid,
      String saleItemUuid,
      Integer quantity,
      BigDecimal unitPrice,
      String idempotencyKey) {
    return OrderQueueMessage.builder()
        .orderUuid(UUID.randomUUID().toString())
        .userUuid(userUuid)
        .saleItemUuid(saleItemUuid)
        .quantity(quantity)
        .unitPrice(unitPrice)
        .totalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)))
        .idempotencyKey(idempotencyKey)
        .build();
  }
}
