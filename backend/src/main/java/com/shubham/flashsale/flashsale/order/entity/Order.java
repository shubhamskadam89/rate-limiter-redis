package com.shubham.flashsale.flashsale.order.entity;

import com.shubham.flashsale.common.entity.BaseEntity;
import com.shubham.flashsale.flashsale.sale.entity.SaleItem;
import com.shubham.flashsale.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "orders")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Order extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sale_item_id", nullable = false)
  private SaleItem saleItem;

  @Column(nullable = false)
  private Integer quantity;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal unitPrice;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal totalPrice;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status = Status.CONFIRMED;

  @Column(nullable = false, unique = true, length = 36)
  private String idempotencyKey;
}
