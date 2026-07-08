package com.shubham.flashsale.exception.sale;

public class SaleAlreadyActiveException extends RuntimeException {

  public SaleAlreadyActiveException(String saleUuid) {
    super("Sale already active: " + saleUuid);
  }
}
