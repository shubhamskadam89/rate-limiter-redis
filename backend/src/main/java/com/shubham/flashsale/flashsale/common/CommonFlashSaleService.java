package com.shubham.flashsale.flashsale.common;

import com.shubham.flashsale.common.service.MetricsService;
import com.shubham.flashsale.exception.purchase.PurchaseLimitExceededException;
import com.shubham.flashsale.exception.purchase.PurchaseNotAllowedException;
import com.shubham.flashsale.exception.purchase.SoldOutException;
import com.shubham.flashsale.exception.sale.InvalidSaleException;
import com.shubham.flashsale.exception.sale.InvalidSaleItemException;
import com.shubham.flashsale.exception.sale.SaleItemNotFoundException;
import com.shubham.flashsale.exception.sale.SaleNotActiveException;
import com.shubham.flashsale.flashsale.order.dto.PurchaseResponse;
import com.shubham.flashsale.flashsale.order.entity.Order;
import com.shubham.flashsale.flashsale.order.queue.OrderQueueMessage;
import com.shubham.flashsale.flashsale.order.repository.OrderRepository;
import com.shubham.flashsale.flashsale.order.service.lua.PurchaseResult;
import com.shubham.flashsale.flashsale.order.service.lua.PurchaseStatus;
import com.shubham.flashsale.flashsale.sale.dto.AddSaleItemRequest;
import com.shubham.flashsale.flashsale.sale.dto.CreateSaleRequest;
import com.shubham.flashsale.flashsale.sale.entity.SaleEvent;
import com.shubham.flashsale.flashsale.sale.entity.SaleItem;
import com.shubham.flashsale.flashsale.sale.entity.Status;
import com.shubham.flashsale.product.entity.Product;
import com.shubham.flashsale.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Slf4j
@AllArgsConstructor
@Service
public class CommonFlashSaleService {

    private final OrderRepository orderRepository;
    private final MetricsService metricsService;

    public SaleEvent validateSaleItemBelongsToSale(
            SaleItem saleItem,
            String saleUuid
    ) {
        SaleEvent saleEvent = saleItem.getSaleEvent();

        log.debug(
                "Validating sale item ownership. saleItemUuid={}, expectedSaleUuid={}, actualSaleUuid={}",
                saleItem.getUuid(),
                saleUuid,
                saleEvent.getUuid()
        );

        if (!saleEvent.getUuid().equals(saleUuid)) {
            log.warn(
                    "Sale item does not belong to requested sale. saleItemUuid={}, expectedSaleUuid={}, actualSaleUuid={}",
                    saleItem.getUuid(),
                    saleUuid,
                    saleEvent.getUuid()
            );
            throw new SaleItemNotFoundException(saleItem.getUuid());
        }

        return saleEvent;
    }

    public void validateSaleEvent(SaleEvent saleEvent) {
        log.debug(
                "Validating sale event. saleUuid={}, status={}",
                saleEvent.getUuid(),
                saleEvent.getStatus()
        );

        if (saleEvent.getStatus() != Status.ACTIVE) {
            log.warn(
                    "Purchase rejected because sale is not active. saleUuid={}, status={}",
                    saleEvent.getUuid(),
                    saleEvent.getStatus()
            );
            throw new SaleNotActiveException();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = saleEvent.getStartTime();
        LocalDateTime endTime = saleEvent.getEndTime();

        if (now.isBefore(startTime)) {
            log.warn(
                    "Purchase rejected because sale has not started. saleUuid={}, startTime={}, now={}",
                    saleEvent.getUuid(),
                    startTime,
                    now
            );
            throw new PurchaseNotAllowedException();
        }

        if (now.isAfter(endTime)) {
            log.warn(
                    "Purchase rejected because sale has ended. saleUuid={}, endTime={}, now={}",
                    saleEvent.getUuid(),
                    endTime,
                    now
            );
            throw new PurchaseNotAllowedException();
        }
    }

    public void validatePurchaseResult(PurchaseResult result) {
        log.debug("Validating Lua purchase result. status={}", result.status());

        if (result.status() == PurchaseStatus.SOLD_OUT) {
            throw new SoldOutException();
        }

        if (result.status() == PurchaseStatus.LIMIT_EXCEEDED) {
            throw new PurchaseLimitExceededException();
        }

        if (result.status() == PurchaseStatus.INVENTORY_NOT_LOADED) {
            throw new PurchaseNotAllowedException();
        }
    }

    public Order persistOrder(
            User user,
            SaleItem saleItem,
            Integer quantity,
            String idempotencyKey
    ) {
        BigDecimal unitPrice = saleItem.getSalePrice();
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));


        Order order = Order.builder()
                .user(user)
                .saleItem(saleItem)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .status(com.shubham.flashsale.flashsale.order.entity.Status.CONFIRMED)
                .idempotencyKey(idempotencyKey)
                .build();

        return orderRepository.save(order);
    }

    public PurchaseResponse buildPurchaseResponse(
            Order savedOrder,
            SaleItem saleItem,
            PurchaseResult result
    ) {
        return PurchaseResponse.builder()
                .orderUuid(UUID.fromString(savedOrder.getUuid()))
                .saleItemUuid(UUID.fromString(saleItem.getUuid()))
                .productUuid(UUID.fromString(saleItem.getProduct().getUuid()))
                .quantity(savedOrder.getQuantity())
                .remainingInventory(result.remainingInventory())
                .message("Purchase successful")
                .build();
    }

    public void validateSaleRequest(CreateSaleRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            log.debug("Validation failed: end time before start time start={} end={}",
                    request.getStartTime(), request.getEndTime());
            throw new InvalidSaleException("End time must be after start time");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            log.debug("Validation failed: start time in the past startTime={}", request.getStartTime());
            throw new InvalidSaleException("Sale cannot start in the past");
        }
    }

    public void validateSaleItemRequest(AddSaleItemRequest request, Product product) {
        if (request.getSalePrice().compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("Validation failed: non-positive sale price={}", request.getSalePrice());
            throw new InvalidSaleItemException("Sale price must be positive");
        }

        if (request.getSalePrice().compareTo(product.getBasePrice()) > 0) {
            log.debug("Validation failed: sale price={} exceeds base price={}",
                    request.getSalePrice(), product.getBasePrice());
            throw new InvalidSaleItemException("Sale price cannot exceed base price");
        }

        if (request.getInventory() <= 0) {
            log.debug("Validation failed: non-positive inventory={}", request.getInventory());
            throw new InvalidSaleItemException("Inventory must be greater than zero");
        }

        if (request.getMaxPerUser() <= 0) {
            log.debug("Validation failed: non-positive maxPerUser={}", request.getMaxPerUser());
            throw new InvalidSaleItemException("Max per user must be greater than zero");
        }
    }

    public PurchaseResponse buildQueuedPurchaseResponse(
            OrderQueueMessage message,
            SaleItem saleItem,
            PurchaseResult result
    ) {
        return PurchaseResponse.builder()
                .orderUuid(UUID.fromString(message.getOrderUuid()))
                .saleItemUuid(UUID.fromString(saleItem.getUuid()))
                .productUuid(UUID.fromString(saleItem.getProduct().getUuid()))
                .quantity(message.getQuantity())
                .remainingInventory(result.remainingInventory())
                .message("Purchase successful. Order queued for persistence")
                .build();
    }
}
