package com.shubham.flashsale.flashsale.order.service;

import com.shubham.flashsale.flashsale.order.dto.OrderResponse;
import java.util.List;

public interface OrderService {

  OrderResponse getOrder(String uuid);

  List<OrderResponse> getAllOrders();

  List<OrderResponse> getCurrentUserOrders();

  List<OrderResponse> getOrdersByUser(String userUuid);
}
