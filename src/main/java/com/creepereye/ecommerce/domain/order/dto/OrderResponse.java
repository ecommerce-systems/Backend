package com.creepereye.ecommerce.domain.order.dto;

import com.creepereye.ecommerce.domain.order.entity.Order;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class OrderResponse {
    private final Long orderId;
    private final LocalDateTime orderDate;
    private final String status;
    private final List<OrderDetailResponse> orderDetails;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .orderDetails(order.getOrderDetails().stream()
                        .map(orderDetail -> OrderDetailResponse.builder()
                                .productId(orderDetail.getProduct().getProductId().longValue())
                                .productName(orderDetail.getProduct().getProdName())
                                .quantity(orderDetail.getQuantity())
                                .price(orderDetail.getPrice())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
