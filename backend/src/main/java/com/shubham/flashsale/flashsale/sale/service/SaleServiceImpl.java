package com.shubham.flashsale.flashsale.sale.service;

import com.shubham.flashsale.common.CommonService;
import com.shubham.flashsale.exception.product.NoSuchProductException;
import com.shubham.flashsale.exception.sale.*;
import com.shubham.flashsale.flashsale.sale.dto.AddSaleItemRequest;
import com.shubham.flashsale.flashsale.sale.dto.CreateSaleRequest;
import com.shubham.flashsale.flashsale.sale.dto.SaleItemResponse;
import com.shubham.flashsale.flashsale.sale.dto.SaleResponse;
import com.shubham.flashsale.flashsale.sale.entity.SaleEvent;
import com.shubham.flashsale.flashsale.sale.entity.SaleItem;
import com.shubham.flashsale.flashsale.sale.entity.Status;
import com.shubham.flashsale.flashsale.sale.repository.SaleEventRepository;
import com.shubham.flashsale.flashsale.sale.repository.SaleItemRepository;
import com.shubham.flashsale.product.entity.Product;
import com.shubham.flashsale.product.repository.ProductRepository;
import com.shubham.flashsale.user.entity.User;
import com.shubham.flashsale.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleEventRepository saleEventRepository;
    private final ProductRepository productRepository;
    private final SaleItemRepository saleItemRepository;
    private final UserRepository userRepository;
    private final CommonService commonService;

    @Override
    @Transactional
    public SaleResponse createSale(CreateSaleRequest request) {

        log.debug("Validating sale request name={} startTime={} endTime={}",
                request.getName(), request.getStartTime(), request.getEndTime());
        validateSaleRequest(request);

        User currentUser = commonService.getCurrentUser();

        log.info("Creating sale campaign name={} by user={}",
                request.getName(), currentUser.getEmail());

        SaleEvent saleEvent = SaleEvent.builder()
                .name(request.getName())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(Status.DRAFT)
                .createdBy(currentUser)
                .build();

        SaleEvent savedSale = saleEventRepository.save(saleEvent);

        log.info("Sale campaign created id={} uuid={}", savedSale.getId(), savedSale.getUuid());

        return SaleResponse.builder()
                .saleUuid(UUID.fromString(savedSale.getUuid()))
                .name(savedSale.getName())
                .startTime(savedSale.getStartTime())
                .endTime(savedSale.getEndTime())
                .status(savedSale.getStatus())
                .build();
    }



    @Override
    @Transactional
    public SaleItemResponse addItemToSale(String saleUuid, AddSaleItemRequest request) {

        log.debug("Adding item to sale saleId={} productId={}", saleUuid, request.getProductUuid());

        SaleEvent saleEvent = saleEventRepository.findByUuid(saleUuid)
                .orElseThrow(() -> {
                    log.warn("Sale event not found saleId={}", saleUuid);
                    return new SaleEventNotFoundException(saleUuid);
                });

        if (saleEvent.getStatus() != Status.DRAFT) {
            log.warn("Attempt to modify non-draft sale saleId={} status={}",
                    saleUuid, saleEvent.getStatus());
            throw new SaleAlreadyActiveException(saleUuid);
        }

        Product product = productRepository.findByUuid(request.getProductUuid())
                .orElseThrow(() -> {
                    log.warn("Product not found productId={}", request.getProductUuid());
                    return new NoSuchProductException(request.getProductUuid());
                });

        log.debug("Validating sale item request productId={} salePrice={} inventory={}",
                product.getId(), request.getSalePrice(), request.getInventory());
        validateSaleItemRequest(request, product);

        if (saleItemRepository.existsBySaleEventAndProduct(saleEvent, product)) {
            log.warn("Duplicate product={} for sale={}", product.getId(), saleUuid);
            throw new DuplicateSaleItemException(saleUuid, product.getUuid());
        }

        SaleItem saleItem = SaleItem.builder()
                .saleEvent(saleEvent)
                .product(product)
                .salePrice(request.getSalePrice())
                .inventory(request.getInventory())
                .finalCount(0L)
                .maxPerUser(request.getMaxPerUser())
                .build();

        SaleItem savedItem = saleItemRepository.save(saleItem);

        log.info("Product={} added to sale={} saleItemId={} uuid={}",
                product.getId(), saleUuid, savedItem.getId(), savedItem.getUuid());

        return SaleItemResponse.builder()

                .saleItemUuid(UUID.fromString(savedItem.getUuid()))
                .saleEventUuid(UUID.fromString(saleEvent.getUuid()))
                .productUuid(UUID.fromString(product.getUuid()))
                .productName(product.getName())
                .salePrice(savedItem.getSalePrice())
                .inventory(savedItem.getInventory())
                .finalCount(savedItem.getFinalCount())
                .maxPerUser(savedItem.getMaxPerUser())
                .build();
    }



    @Override
    public SaleResponse activateSale(String saleUuid) {
        log.warn("activateSale called but not yet implemented saleId={}", saleUuid);
        throw new UnsupportedOperationException("Will be implemented in Issue #11");
    }



    @Override
    public SaleResponse getSale(String saleUuid) {
        log.debug("getSale called saleId={}", saleUuid);
        return null;
    }



    private void validateSaleRequest(CreateSaleRequest request) {
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

    private void validateSaleItemRequest(AddSaleItemRequest request, Product product) {
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
}