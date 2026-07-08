package com.shubham.flashsale.exception.sale;

public class SaleEventNotFoundException extends RuntimeException {

  public SaleEventNotFoundException(String saleUuid) {
    super("Sale event not found: " + saleUuid);
  }
}
