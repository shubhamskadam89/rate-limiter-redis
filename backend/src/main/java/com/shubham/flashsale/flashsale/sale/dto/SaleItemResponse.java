package com.shubham.flashsale.flashsale.sale.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemResponse {

  private UUID saleItemUuid;

  private UUID saleEventUuid;

  private UUID productUuid;

  private String productName;

  private BigDecimal salePrice;

  private Long inventory;

  private Long finalCount;

  private Integer maxPerUser;
}
