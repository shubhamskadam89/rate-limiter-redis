package com.shubham.flashsale.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> userAlreadyExist(
            UserAlreadyExistsException ex,
            HttpServletRequest request){
       ApiErrorResponse apiErrorResponse  = new ApiErrorResponse(
               Instant.now(),
               HttpStatus.CONFLICT,
               ex.getMessage(),
               "USER_WITH_MAIL_ALREADY_EXIST",
               request.getRequestURI(),
               null
       );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiErrorResponse);
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> refreshTokenNotFound(
            RefreshTokenNotFoundException ex,
            HttpServletRequest request
    ){
        ApiErrorResponse response =
                new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.UNAUTHORIZED,
                        ex.getMessage(),
                        "REFRESH_TOKEN_NOT_FOUND",
                        request.getRequestURI(),
                        null
                );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ApiErrorResponse> refreshTokenExpired(
            RefreshTokenExpiredException ex,
            HttpServletRequest request
    ){
        ApiErrorResponse response =
                new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.UNAUTHORIZED,
                        ex.getMessage(),
                        "REFRESH_TOKEN_EXPIRED",
                        request.getRequestURI(),
                        null
                );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(RefreshTokenRevokedException.class)
    public ResponseEntity<ApiErrorResponse> refreshTokenRevoked(
            RefreshTokenRevokedException ex,
            HttpServletRequest request
    ){
        ApiErrorResponse response =
                new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.UNAUTHORIZED,
                        ex.getMessage(),
                        "REFRESH_TOKEN_REVOKED",
                        request.getRequestURI(),
                        null
                );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

}