package com.shubham.flashsale.exception.inventory;

public class InventoryStateUnavailableException extends RuntimeException {

  public InventoryStateUnavailableException(String saleItemUuid) {
    super("Inventory state is temporarily unavailable for sale item " + saleItemUuid);
  }
}
