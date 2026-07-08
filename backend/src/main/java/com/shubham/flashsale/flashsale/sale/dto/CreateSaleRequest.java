package com.shubham.flashsale.flashsale.sale.dto;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSaleRequest {

  private String name;

  private LocalDateTime startTime;

  private LocalDateTime endTime;
}
