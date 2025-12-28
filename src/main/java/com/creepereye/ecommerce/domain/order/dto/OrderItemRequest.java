package com.creepereye.ecommerce.domain.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {
    private Integer productId;
    private Integer quantity;
}
