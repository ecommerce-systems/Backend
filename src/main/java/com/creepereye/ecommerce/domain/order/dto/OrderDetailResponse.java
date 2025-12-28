package com.creepereye.ecommerce.domain.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderDetailResponse {
    private final Long productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal price;
}
