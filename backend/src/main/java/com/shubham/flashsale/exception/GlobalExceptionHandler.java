package com.shubham.flashsale.exception;

import com.shubham.flashsale.exception.inventory.InventoryStateUnavailableException;
import com.shubham.flashsale.exception.product.NoSuchProductException;
import com.shubham.flashsale.exception.purchase.*;
import com.shubham.flashsale.exception.sale.*;
import com.shubham.flashsale.exception.security.*;
import com.shubham.flashsale.exception.user.UserAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // ── User ────────────────────────────────────────────────────────────────

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ApiErrorResponse> userAlreadyExist(
      UserAlreadyExistsException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), "USER_ALREADY_EXISTS", request);
  }

  // ── Security / Auth ──────────────────────────────────────────────────────

  @ExceptionHandler(RefreshTokenNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> refreshTokenNotFound(
      RefreshTokenNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), "REFRESH_TOKEN_NOT_FOUND", request);
  }

  @ExceptionHandler(RefreshTokenExpiredException.class)
  public ResponseEntity<ApiErrorResponse> refreshTokenExpired(
      RefreshTokenExpiredException ex, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), "REFRESH_TOKEN_EXPIRED", request);
  }

  @ExceptionHandler(RefreshTokenRevokedException.class)
  public ResponseEntity<ApiErrorResponse> refreshTokenRevoked(
      RefreshTokenRevokedException ex, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), "REFRESH_TOKEN_REVOKED", request);
  }

  // ── Product ──────────────────────────────────────────────────────────────

  @ExceptionHandler(NoSuchProductException.class)
  public ResponseEntity<ApiErrorResponse> noSuchProduct(
      NoSuchProductException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), "PRODUCT_NOT_FOUND", request);
    // was BAD_REQUEST — 404 is correct for a missing resource
  }

  // ── Sale ─────────────────────────────────────────────────────────────────

  @ExceptionHandler(SaleEventNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> saleEventNotFound(
      SaleEventNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), "SALE_EVENT_NOT_FOUND", request);
  }

  @ExceptionHandler(SaleItemNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> saleItemNotFound(
      SaleItemNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), "SALE_ITEM_NOT_FOUND", request);
  }

  @ExceptionHandler(SaleAlreadyActiveException.class)
  public ResponseEntity<ApiErrorResponse> saleAlreadyActive(
      SaleAlreadyActiveException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), "SALE_ALREADY_ACTIVE", request);
  }

  @ExceptionHandler(DuplicateSaleItemException.class)
  public ResponseEntity<ApiErrorResponse> duplicateSaleItem(
      DuplicateSaleItemException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), "DUPLICATE_SALE_ITEM", request);
  }

  @ExceptionHandler(InvalidSaleException.class)
  public ResponseEntity<ApiErrorResponse> invalidSale(
      InvalidSaleException ex, HttpServletRequest request) {
    return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), "INVALID_SALE", request);
  }

  @ExceptionHandler(InvalidSaleItemException.class)
  public ResponseEntity<ApiErrorResponse> invalidSaleItem(
      InvalidSaleItemException ex, HttpServletRequest request) {
    return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), "INVALID_SALE_ITEM", request);
  }

  // ── Purchase ─────────────────────────────────────────────────────────────

  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> orderNotFound(
      OrderNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), "ORDER_NOT_FOUND", request);
  }

  @ExceptionHandler(SoldOutException.class)
  public ResponseEntity<ApiErrorResponse> soldOut(SoldOutException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), "SOLD_OUT", request);
  }

  @ExceptionHandler(PurchaseLimitExceededException.class)
  public ResponseEntity<ApiErrorResponse> purchaseLimitExceeded(
      PurchaseLimitExceededException ex, HttpServletRequest request) {
    return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), "PURCHASE_LIMIT_EXCEEDED", request);
  }

  @ExceptionHandler(PurchaseNotAllowedException.class)
  public ResponseEntity<ApiErrorResponse> purchaseNotAllowed(
      PurchaseNotAllowedException ex, HttpServletRequest request) {
    return build(HttpStatus.FORBIDDEN, ex.getMessage(), "PURCHASE_NOT_ALLOWED", request);
  }

  // ── Inventory ───────────────────────────────────────────────────────────

  @ExceptionHandler(InventoryStateUnavailableException.class)
  public ResponseEntity<ApiErrorResponse> inventoryStateUnavailable(
      InventoryStateUnavailableException ex, HttpServletRequest request) {
    return build(
        HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), "INVENTORY_STATE_UNAVAILABLE", request);
  }

  // ── Fallback ─────────────────────────────────────────────────────────────

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpected(
      Exception ex, HttpServletRequest request) {
    log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return build(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred",
        "INTERNAL_ERROR",
        request);
  }

  // ── Helper ───────────────────────────────────────────────────────────────

  private ResponseEntity<ApiErrorResponse> build(
      HttpStatus status, String msg, String errorCode, HttpServletRequest request) {
    ApiErrorResponse body =
        new ApiErrorResponse(Instant.now(), status, msg, errorCode, request.getRequestURI(), null);
    return ResponseEntity.status(status).body(body);
  }
}
