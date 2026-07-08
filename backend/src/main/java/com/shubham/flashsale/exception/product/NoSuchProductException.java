package com.shubham.flashsale.exception.product;

public class NoSuchProductException extends RuntimeException {

  public NoSuchProductException(String productUuid) {

    super("Product not found: " + productUuid);
  }
}
