package com.shubham.flashsale.flashsale.order.service;

import com.shubham.flashsale.exception.sale.SaleItemNotFoundException;
import com.shubham.flashsale.flashsale.order.entity.Order;
import com.shubham.flashsale.flashsale.order.queue.OrderQueueMessage;
import com.shubham.flashsale.flashsale.order.repository.OrderRepository;
import com.shubham.flashsale.flashsale.sale.entity.SaleItem;
import com.shubham.flashsale.flashsale.sale.repository.SaleItemRepository;
import com.shubham.flashsale.user.entity.User;
import com.shubham.flashsale.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderPersistenceServiceImpl implements OrderPersistenceService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SaleItemRepository saleItemRepository;


    @Override
    public void persist(OrderQueueMessage message) {

        if (orderRepository.findByIdempotencyKey(message.getIdempotencyKey()).isPresent()) {
            log.info(
                    "Skipping duplicate order persistence. orderUuid={}, idempotencyKey={}",
                    message.getOrderUuid(),
                    message.getIdempotencyKey()
            );
            return;
        }

        User user = userRepository.findByUuid(message.getUserUuid())
                .orElseThrow(() -> new AccessDeniedException("User not found: " + message.getUserUuid()));

        SaleItem saleItem = saleItemRepository.findByUuid(message.getSaleItemUuid())
                .orElseThrow(() -> new SaleItemNotFoundException(message.getSaleItemUuid()));

        Order order = Order.builder()
                .user(user)
                .saleItem(saleItem)
                .quantity(message.getQuantity())
                .unitPrice(message.getUnitPrice())
                .totalPrice(message.getTotalPrice())
                .status(com.shubham.flashsale.flashsale.order.entity.Status.CONFIRMED)
                .idempotencyKey(message.getIdempotencyKey())
                .build();

        order.setUuid(message.getOrderUuid());

        Order savedOrder = orderRepository.save(order);

        log.info(
                "Order persisted from queue. orderUuid={}, userUuid={}, saleItemUuid={}",
                savedOrder.getUuid(),
                user.getUuid(),
                saleItem.getUuid()
        );
    }
}
