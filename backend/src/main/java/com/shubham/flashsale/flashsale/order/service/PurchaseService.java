package com.shubham.flashsale.flashsale.order.service;

import com.shubham.flashsale.flashsale.order.dto.PurchaseRequest;
import com.shubham.flashsale.flashsale.order.dto.PurchaseResponse;

public interface PurchaseService {

  PurchaseResponse purchase(
      String saleUuid, String saleItemUuid, String idempotencyKey, PurchaseRequest request);
}
