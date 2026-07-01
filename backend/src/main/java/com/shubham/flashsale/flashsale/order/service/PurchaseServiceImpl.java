package com.shubham.flashsale.flashsale.order.service;

import com.shubham.flashsale.common.CommonAuthService;
import com.shubham.flashsale.exception.sale.SaleItemNotFoundException;
import com.shubham.flashsale.flashsale.common.CommonFlashSaleService;
import com.shubham.flashsale.flashsale.order.dto.PurchaseRequest;
import com.shubham.flashsale.flashsale.order.dto.PurchaseResponse;
import com.shubham.flashsale.flashsale.order.entity.Order;
import com.shubham.flashsale.flashsale.order.queue.OrderQueueMessage;
import com.shubham.flashsale.flashsale.order.queue.OrderQueueProducer;
import com.shubham.flashsale.flashsale.order.repository.OrderRepository;
import com.shubham.flashsale.flashsale.order.service.lua.FlashSalePurchaseExecutor;
import com.shubham.flashsale.flashsale.order.service.lua.PurchaseResult;
import com.shubham.flashsale.flashsale.sale.entity.SaleEvent;
import com.shubham.flashsale.flashsale.sale.entity.SaleItem;
import com.shubham.flashsale.flashsale.sale.repository.SaleItemRepository;
import com.shubham.flashsale.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final OrderRepository orderRepository;
    private final FlashSalePurchaseExecutor purchaseExecutor;
    private final CommonAuthService commonAuthService;
    private final SaleItemRepository saleItemRepository;
    private final CommonFlashSaleService commonFlashSaleService;
    private final OrderQueueProducer orderQueueProducer;

    @Override
    @Transactional
    public PurchaseResponse purchase(
            String saleUuid,
            String saleItemUuid,
            String idempotencyKey,
            PurchaseRequest request
    ) {
        log.info("Initiating flash sale purchase. SaleUUID: {}, ItemUUID: {}, IdempotencyKey: {}, RequestedQty: {}",
                saleUuid, saleItemUuid, idempotencyKey, request.getQuantity());

        SaleItem saleItem = saleItemRepository.findByUuid(saleItemUuid)
                .orElseThrow(() -> {
                    log.warn("Purchase failed: Sale item not found. ItemUUID: {}", saleItemUuid);
                    return new SaleItemNotFoundException(saleItemUuid);
                });

        log.debug("Validating item membership and event status for ItemUUID: {}", saleItemUuid);
        SaleEvent saleEvent = commonFlashSaleService.validateSaleItemBelongsToSale(saleItem, saleUuid);
        commonFlashSaleService.validateSaleEvent(saleEvent);

        User user = commonAuthService.getCurrentUser();
        log.debug("Executing Lua script token bucket/inventory check for UserUUID: {}, ItemUUID: {}", user.getUuid(), saleItem.getUuid());

        PurchaseResult result = purchaseExecutor.execute(
                saleItem.getUuid(),
                user.getUuid(),
                request.getQuantity(),
                saleItem.getMaxPerUser()
        );

        log.debug("Lua execution finished. Result status: {}", result.status());
        commonFlashSaleService.validatePurchaseResult(result);

        log.info("Inventory reserved successfully in Redis. Queueing order for asynchronous persistence. userUuid={}", user.getUuid());

        OrderQueueMessage message = OrderQueueMessage.create(
                user.getUuid(),
                saleItem.getUuid(),
                request.getQuantity(),
                saleItem.getSalePrice(),
                idempotencyKey
        );

        orderQueueProducer.enqueue(message);

        log.info(
                "Purchase flow completed successfully. Order queued. orderUuid={}, userUuid={}, saleItemUuid={}",
                message.getOrderUuid(),
                user.getUuid(),
                saleItem.getUuid()
        );

        return commonFlashSaleService.buildQueuedPurchaseResponse(
                message,
                saleItem,
                result
        );
    }
}
