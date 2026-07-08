package com.shubham.flashsale.flashsale.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {

  @NotNull
  @Min(1)
  private Integer quantity;
}
