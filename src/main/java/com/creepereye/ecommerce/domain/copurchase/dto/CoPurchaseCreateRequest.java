package com.creepereye.ecommerce.domain.copurchase.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CoPurchaseCreateRequest {

    @NotNull(message = "소스 상품 ID는 필수입니다.")
    private Long sourceProductId;

    @NotNull(message = "대상 상품 ID는 필수입니다.")
    private Long targetProductId;

    @NotNull(message = "점수는 필수입니다.")
    private Double score;
}
