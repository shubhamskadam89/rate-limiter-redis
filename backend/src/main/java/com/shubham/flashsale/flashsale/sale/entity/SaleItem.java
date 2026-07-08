package com.shubham.flashsale.flashsale.sale.entity;

import com.shubham.flashsale.common.entity.BaseEntity;
import com.shubham.flashsale.product.entity.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sale_items")
public class SaleItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sale_event_id", nullable = false)
  private SaleEvent saleEvent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "sale_price", nullable = false)
  private BigDecimal salePrice;

  @Column(nullable = false)
  private Long inventory;

  @Column(name = "final_count")
  private Long finalCount;

  @Column(name = "max_per_user", nullable = false)
  private Integer maxPerUser;
}
