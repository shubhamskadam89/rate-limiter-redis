package com.shubham.flashsale.flashsale.order.service;

import com.shubham.flashsale.common.CommonService;
import com.shubham.flashsale.exception.purchase.PurchaseNotAllowedException;
import com.shubham.flashsale.exception.sale.SaleItemNotFoundException;
import com.shubham.flashsale.exception.sale.SaleNotActiveException;
import com.shubham.flashsale.flashsale.order.dto.PurchaseRequest;
import com.shubham.flashsale.flashsale.order.dto.PurchaseResponse;
import com.shubham.flashsale.flashsale.order.repository.OrderRepository;
import com.shubham.flashsale.flashsale.order.service.lua.FlashSalePurchaseExecutor;
import com.shubham.flashsale.flashsale.order.service.lua.PurchaseResult;
import com.shubham.flashsale.flashsale.sale.entity.SaleEvent;
import com.shubham.flashsale.flashsale.sale.entity.Status;
import com.shubham.flashsale.flashsale.sale.repository.SaleEventRepository;
import com.shubham.flashsale.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;


@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final SaleEventRepository saleRepository;

    private final OrderRepository orderRepository;

    private final FlashSalePurchaseExecutor purchaseExecutor;
    private final CommonService commonService;

//    private final ModelMapper mapper;

    @Override
    public PurchaseResponse purchase(String saleUuid, String saleItemUuid, PurchaseRequest request)
    {

        SaleEvent event = saleRepository.findByUuid(saleItemUuid)
                .orElseThrow(()-> new SaleItemNotFoundException(saleItemUuid));

        if (event.getStatus() != Status.ACTIVE) {
            throw new SaleNotActiveException();
        }

        Instant now = Instant.now();

        if (now.isBefore(event.getStartTime().toInstant(ZoneOffset.UTC))) {
            throw new PurchaseNotAllowedException();
        }

        if (now.isAfter(event.getEndTime().toInstant(ZoneOffset.UTC))) {
            throw new PurchaseNotAllowedException();
        }

        // TODO: Issue #11 - Reimplement purchase flow using SaleItem.
        throw new UnsupportedOperationException(
                "Purchase flow will be implemented in Issue #11"
        );
    }


}
