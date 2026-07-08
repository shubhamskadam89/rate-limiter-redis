package com.shubham.flashsale.exception;

import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public class ApiErrorResponse {
  private final Instant timestamp;
  private final HttpStatus status;
  private final String msg;
  private final String error;
  private final String path;
  private final List<String> details;
}
