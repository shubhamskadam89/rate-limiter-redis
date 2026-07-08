package com.shubham.flashsale.exception.sale;

public class SaleItemNotFoundException extends RuntimeException {

  public SaleItemNotFoundException(String saleItemUuid) {
    super("Sale item not found: " + saleItemUuid);
  }
}
