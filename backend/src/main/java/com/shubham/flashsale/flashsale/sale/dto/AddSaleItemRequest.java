package com.shubham.flashsale.flashsale.sale.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddSaleItemRequest {
    private String productUuid;

    private BigDecimal salePrice;

    private Long inventory;

    private Integer maxPerUser;
}