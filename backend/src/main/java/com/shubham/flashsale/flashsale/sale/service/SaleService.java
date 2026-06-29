package com.shubham.flashsale.flashsale.sale.service;


import com.shubham.flashsale.flashsale.sale.dto.AddSaleItemRequest;
import com.shubham.flashsale.flashsale.sale.dto.CreateSaleRequest;
import com.shubham.flashsale.flashsale.sale.dto.SaleItemResponse;
import com.shubham.flashsale.flashsale.sale.dto.SaleResponse;

public interface SaleService {


    SaleResponse createSale(
            CreateSaleRequest request
    );

    SaleItemResponse addItemToSale(
            String saleUuid,
            AddSaleItemRequest request
    );

    SaleResponse activateSale(
            String saleUuid
    );

    SaleResponse getSale(String saleUuid);
}