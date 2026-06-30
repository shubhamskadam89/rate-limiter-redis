package com.shubham.flashsale.product.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private UUID uuid;

    private String name;

    private String description;

    private BigDecimal basePrice;

    private java.util.Map<String, Object> metadata;

    private Boolean isActive;
}