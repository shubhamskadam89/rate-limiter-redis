package com.shubham.flashsale.exception.sale;

public class DuplicateSaleItemException extends RuntimeException {

  public DuplicateSaleItemException(String saleUuid, String productUuid) {
    super("Product " + productUuid + " already exists in sale " + saleUuid);
  }
}
