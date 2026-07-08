package com.shubham.flashsale.flashsale.sale.dto;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddSaleItemRequest {
  private String productUuid;

  private BigDecimal salePrice;

  private Long inventory;

  private Integer maxPerUser;
}
