package com.shubham.flashsale.product.service;

import com.shubham.flashsale.product.dto.CreateProductRequest;
import com.shubham.flashsale.product.dto.ProductResponse;
import java.util.List;

public interface ProductService {

  ProductResponse createProduct(CreateProductRequest request);

  ProductResponse getProduct(String productUuid);

  List<ProductResponse> getAllProducts();
}
