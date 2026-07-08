package com.shubham.flashsale.common.cache;

import java.time.Duration;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CacheTtl {

  public static final Duration PRODUCTS = Duration.ofMinutes(10);

  public static final Duration PRODUCT = Duration.ofMinutes(10);

  public static final Duration SALE = Duration.ofMinutes(2);

  public static final Duration SALE_DETAIL = Duration.ofMinutes(2);

  public static final Duration ADMIN_SALES = Duration.ofMinutes(2);

  public static final Duration AVAILABLE_SALES = Duration.ofMinutes(2);

  public static final Duration SALE_ITEMS = Duration.ofMinutes(2);
}
