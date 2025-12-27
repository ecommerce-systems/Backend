package com.creepereye.ecommerce.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponseDto {
    private Integer productId;
    private String prodName;
}
