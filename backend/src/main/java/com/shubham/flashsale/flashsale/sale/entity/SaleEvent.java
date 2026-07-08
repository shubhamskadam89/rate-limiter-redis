package com.shubham.flashsale.flashsale.sale.entity;

import com.shubham.flashsale.common.entity.BaseEntity;
import com.shubham.flashsale.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sale_events")
public class SaleEvent extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalDateTime endTime;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", nullable = false)
  private User createdBy;

  @OneToMany(mappedBy = "saleEvent", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SaleItem> saleItems = new ArrayList<>();
}
