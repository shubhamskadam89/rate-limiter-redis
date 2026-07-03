package com.shubham.flashsale.flashsale.order.service;

import com.shubham.flashsale.common.service.CommonAuthService;
import com.shubham.flashsale.exception.purchase.OrderNotFoundException;
import com.shubham.flashsale.flashsale.order.dto.OrderResponse;
import com.shubham.flashsale.flashsale.order.entity.Order;
import com.shubham.flashsale.flashsale.order.repository.OrderRepository;
import com.shubham.flashsale.user.entity.User;
import com.shubham.flashsale.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CommonAuthService commonAuthService;
    private final UserRepository userRepository;

    @Override
    public OrderResponse getOrder(String uuid) {
        log.debug("Fetching order by uuid={}", uuid);
        Order order = orderRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.warn("Order not found with uuid={}", uuid);
                    return new OrderNotFoundException(uuid);
                });

        return toOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        log.debug("Fetching all orders");
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getCurrentUserOrders() {
        log.debug("Fetching orders for current user");
        User currentUser = commonAuthService.getCurrentUser();

        return orderRepository.findByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> getOrdersByUser(String userUuid) {
        log.debug("Fetching orders for userUuid={}", userUuid);
        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> {
                    log.warn("User not found when fetching orders for userUuid={}", userUuid);
                    return new OrderNotFoundException(userUuid);
                });

        return orderRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    private OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderUuid(UUID.fromString(order.getUuid()))
                .saleItemUuid(UUID.fromString(order.getSaleItem().getUuid()))
                .productUuid(UUID.fromString(order.getSaleItem().getProduct().getUuid()))
                .productName(order.getSaleItem().getProduct().getName())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt().toInstant(ZoneOffset.UTC))
                .build();
    }
}
