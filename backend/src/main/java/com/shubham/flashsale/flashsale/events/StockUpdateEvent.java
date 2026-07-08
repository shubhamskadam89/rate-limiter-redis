package com.shubham.flashsale.flashsale.events;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateEvent {

  private String saleUuid;

  private String saleItemUuid;

  private Integer remainingInventory;
}
