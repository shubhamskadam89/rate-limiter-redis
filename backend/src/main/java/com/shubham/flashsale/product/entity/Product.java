package com.shubham.flashsale.product.entity;

import com.shubham.flashsale.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Map;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(
    name = "products",
    indexes = {
      @Index(name = "idx_uuid", columnList = "uuid"),
      @Index(name = "idx_name", columnList = "name"),
      @Index(name = "idx_deleted_at", columnList = "deleted_at")
    })
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Product extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal basePrice;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "json")
  private Map<String, Object> metadata;

  @Column(nullable = false)
  private Boolean isActive = true;
}
