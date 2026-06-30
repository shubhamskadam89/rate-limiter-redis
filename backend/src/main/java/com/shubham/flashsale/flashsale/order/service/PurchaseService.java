package com.shubham.flashsale.flashsale.order.service;


import com.shubham.flashsale.exception.sale.SaleNotActiveException;
import com.shubham.flashsale.flashsale.order.dto.PurchaseRequest;
import com.shubham.flashsale.flashsale.order.dto.PurchaseResponse;

public interface PurchaseService {

    PurchaseResponse purchase(
            String saleUuid,
            String saleItemUuid,
            PurchaseRequest request
    );

}