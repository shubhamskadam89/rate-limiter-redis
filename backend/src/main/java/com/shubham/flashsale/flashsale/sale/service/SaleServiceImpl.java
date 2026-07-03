package com.shubham.flashsale.flashsale.sale.service;

import com.shubham.flashsale.common.cache.CacheNames;
import com.shubham.flashsale.common.service.CommonAuthService;
import com.shubham.flashsale.common.redis.RedisKeyBuilder;
import com.shubham.flashsale.exception.product.NoSuchProductException;
import com.shubham.flashsale.exception.sale.DuplicateSaleItemException;
import com.shubham.flashsale.exception.sale.InvalidSaleException;
import com.shubham.flashsale.exception.sale.SaleAlreadyActiveException;
import com.shubham.flashsale.exception.sale.SaleEventNotFoundException;
import com.shubham.flashsale.flashsale.common.CommonFlashSaleService;
import com.shubham.flashsale.flashsale.sale.dto.AddSaleItemRequest;
import com.shubham.flashsale.flashsale.sale.dto.CreateSaleRequest;
import com.shubham.flashsale.flashsale.sale.dto.SaleDetailResponse;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleEventRepository saleEventRepository;
    private final ProductRepository productRepository;
    private final SaleItemRepository saleItemRepository;
    private final UserRepository userRepository;
    private final CommonAuthService commonAuthService;
    private final CommonFlashSaleService commonFlashSaleService;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public SaleResponse createSale(CreateSaleRequest request) {

        log.debug("Validating sale request name={} startTime={} endTime={}",
                request.getName(), request.getStartTime(), request.getEndTime());
        commonFlashSaleService.validateSaleRequest(request);

        User currentUser = commonAuthService.getCurrentUser();

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

        log.info("Sale campaign created uuid={}", savedSale.getUuid());

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
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.SALE, key = "#saleUuid"),
            @CacheEvict(cacheNames = CacheNames.SALE_DETAIL, key = "#saleUuid"),
            @CacheEvict(cacheNames = CacheNames.ADMIN_SALES, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.SALE_ITEMS, key = "#saleUuid"),
            @CacheEvict(cacheNames = CacheNames.AVAILABLE_SALES, allEntries = true)
    })
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
                product.getUuid(), request.getSalePrice(), request.getInventory());
        commonFlashSaleService.validateSaleItemRequest(request, product);

        if (saleItemRepository.existsBySaleEventAndProduct(saleEvent, product)) {
            log.warn("Duplicate product={} for sale={}", product.getUuid(), saleUuid);
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

        log.info("Product={} added to sale={}  uuid={}",
                product.getUuid(), saleUuid, savedItem.getUuid());

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
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.SALE, key = "#saleUuid"),
            @CacheEvict(cacheNames = CacheNames.SALE_DETAIL, key = "#saleUuid"),
            @CacheEvict(cacheNames = CacheNames.ADMIN_SALES, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.SALE_ITEMS, key = "#saleUuid"),
            @CacheEvict(cacheNames = CacheNames.AVAILABLE_SALES, allEntries = true)
    })
    public SaleResponse activateSale(String saleUuid) {
        log.info("Activating sale saleUuid={}", saleUuid);

        SaleEvent saleEvent = saleEventRepository.findByUuid(saleUuid)
                .orElseThrow(() -> new SaleEventNotFoundException(saleUuid));

        if (saleEvent.getStatus() == Status.ACTIVE) {
            log.info("Sale is already active; returning current state without reinitializing inventory saleUuid={}", saleUuid);
            return SaleResponse.builder()
                    .saleUuid(UUID.fromString(saleEvent.getUuid()))
                    .name(saleEvent.getName())
                    .startTime(saleEvent.getStartTime())
                    .endTime(saleEvent.getEndTime())
                    .status(saleEvent.getStatus())
                    .build();
        }

        if (saleEvent.getStatus() != Status.DRAFT) {
            throw new InvalidSaleException("Sale is not in draft state");
        }

        List<SaleItem> saleItems = saleEvent.getSaleItems();

        if (saleItems == null || saleItems.isEmpty()) {
            throw new InvalidSaleException("Sale must contain at least one item");
        }

        saleItems.forEach(saleItem -> {
            if (saleItem.getInventory() == null || saleItem.getInventory() <= 0) {
                throw new InvalidSaleException("Sale item inventory must be positive before activation");
            }

            if (saleItem.getSalePrice() == null || saleItem.getSalePrice().signum() <= 0) {
                throw new InvalidSaleException("Sale item price must be positive before activation");
            }

            if (!Boolean.TRUE.equals(saleItem.getProduct().getIsActive())) {
                throw new InvalidSaleException("Inactive products cannot be activated for sale");
            }

            String inventoryKey = RedisKeyBuilder.inventory(saleItem.getUuid());

            Boolean initialized = redisTemplate.opsForValue().setIfAbsent(
                    inventoryKey,
                    String.valueOf(saleItem.getInventory())
            );

            if (Boolean.FALSE.equals(initialized)) {
                log.info(
                        "Redis inventory already exists; leaving current value unchanged. saleUuid={}, saleItemUuid={}, inventoryKey={}",
                        saleEvent.getUuid(),
                        saleItem.getUuid(),
                        inventoryKey
                );
                return;
            }

            log.info(
                    "Loaded sale item inventory into Redis. saleUuid={}, saleItemUuid={}, inventoryKey={}, inventory={}",
                    saleEvent.getUuid(),
                    saleItem.getUuid(),
                    inventoryKey,
                    saleItem.getInventory()
            );
        });



        saleEvent.setStatus(Status.ACTIVE);
        SaleEvent savedSale = saleEventRepository.save(saleEvent);

        return SaleResponse.builder()
                .saleUuid(UUID.fromString(savedSale.getUuid()))
                .name(savedSale.getName())
                .startTime(savedSale.getStartTime())
                .endTime(savedSale.getEndTime())
                .status(savedSale.getStatus())
                .build();
    }


    @Override
    @Cacheable(
            cacheNames = CacheNames.SALE,
            key = "#saleUuid"
    )
    public SaleResponse getSale(String saleUuid) {
        log.debug("getSale called saleId={}", saleUuid);
        SaleEvent saleEvent = saleEventRepository.findByUuid(saleUuid)
                .orElseThrow(()-> new SaleEventNotFoundException(saleUuid));

        return SaleResponse.builder()
                .saleUuid(UUID.fromString(saleEvent.getUuid()))
                .name(saleEvent.getName())
                .startTime(saleEvent.getStartTime())
                .endTime(saleEvent.getEndTime())
                .status(saleEvent.getStatus())
                .build();
    }

    @Override
    @Transactional
    @Cacheable(
            cacheNames = CacheNames.SALE_DETAIL,
            key = "#saleUuid"
    )
    public SaleDetailResponse getSaleDetail(String saleUuid) {
        SaleEvent saleEvent = saleEventRepository.findByUuid(saleUuid)
                .orElseThrow(() -> new SaleEventNotFoundException(saleUuid));

        return toSaleDetailResponse(saleEvent);
    }

    @Override
    @Transactional
    @Cacheable(
            cacheNames = CacheNames.ADMIN_SALES
    )
    public List<SaleDetailResponse> getAdminSales() {
        User currentUser = commonAuthService.getCurrentUser();

        if (currentUser.getRole().name().equals("ADMIN")) {
            return saleEventRepository.findAllByOrderByCreatedAtDesc()
                    .stream()
                    .map(this::toSaleDetailResponse)
                    .toList();
        }

        return saleEventRepository.findByCreatedByOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(this::toSaleDetailResponse)
                .toList();
    }

    @Override
    @Transactional
    @Cacheable(
            cacheNames = CacheNames.AVAILABLE_SALES
    )
    public List<SaleDetailResponse> getAvailableSales() {
        return saleEventRepository.findByStatus(Status.ACTIVE)
                .stream()
                .map(this::toSaleDetailResponse)
                .toList();
    }

    @Override
    @Transactional
    @Cacheable(
            cacheNames = CacheNames.SALE_ITEMS,
            key = "#saleUuid"
    )
    public List<SaleItemResponse> getSaleItems(String saleUuid) {
        SaleEvent saleEvent = saleEventRepository.findByUuid(saleUuid)
                .orElseThrow(() -> new SaleEventNotFoundException(saleUuid));

        return saleItemRepository.findBySaleEvent(saleEvent)
                .stream()
                .map(this::toSaleItemResponse)
                .toList();
    }

    private SaleDetailResponse toSaleDetailResponse(SaleEvent saleEvent) {
        return SaleDetailResponse.builder()
                .saleUuid(UUID.fromString(saleEvent.getUuid()))
                .name(saleEvent.getName())
                .startTime(saleEvent.getStartTime())
                .endTime(saleEvent.getEndTime())
                .status(saleEvent.getStatus())
                .items(saleEvent.getSaleItems()
                        .stream()
                        .map(this::toSaleItemResponse)
                        .toList())
                .build();
    }

    private SaleItemResponse toSaleItemResponse(SaleItem saleItem) {
        Product product = saleItem.getProduct();

        return SaleItemResponse.builder()
                .saleItemUuid(UUID.fromString(saleItem.getUuid()))
                .saleEventUuid(UUID.fromString(saleItem.getSaleEvent().getUuid()))
                .productUuid(UUID.fromString(product.getUuid()))
                .productName(product.getName())
                .salePrice(saleItem.getSalePrice())
                .inventory(saleItem.getInventory())
                .finalCount(saleItem.getFinalCount())
                .maxPerUser(saleItem.getMaxPerUser())
                .build();
    }



}
