package com.shubham.flashsale.flashsale.sale.service;

import com.shubham.flashsale.flashsale.sale.dto.AddSaleItemRequest;
import com.shubham.flashsale.flashsale.sale.dto.CreateSaleRequest;
import com.shubham.flashsale.flashsale.sale.dto.SaleDetailResponse;
import com.shubham.flashsale.flashsale.sale.dto.SaleItemResponse;
import com.shubham.flashsale.flashsale.sale.dto.SaleResponse;
import java.util.List;

public interface SaleService {

  SaleResponse createSale(CreateSaleRequest request);

  SaleItemResponse addItemToSale(String saleUuid, AddSaleItemRequest request);

  SaleResponse activateSale(String saleUuid);

  SaleResponse getSale(String saleUuid);

  SaleDetailResponse getSaleDetail(String saleUuid);

  List<SaleDetailResponse> getAdminSales();

  List<SaleDetailResponse> getAvailableSales();

  List<SaleItemResponse> getSaleItems(String saleUuid);
}
