package com.shubham.flashsale.idempotency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class IdempotencyRecord {

    private IdempotencyState state;


    private String response;

    private HttpStatus status;


}