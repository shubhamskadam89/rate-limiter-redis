package com.shubham.flashsale.flashsale.order.service;


import com.shubham.flashsale.flashsale.order.dto.OrderResponse;

public interface OrderService {

    OrderResponse getOrder(String uuid);
}