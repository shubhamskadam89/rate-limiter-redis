package com.shubham.flashsale.idempotency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IdempotencyRecord {

  private IdempotencyState state;

  private String responseBody;

  private int statusCode;
}
