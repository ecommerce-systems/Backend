package com.creepereye.ecommerce.domain.copurchase.dto;

import com.creepereye.ecommerce.domain.product.entity.Product;
import lombok.Getter;


import java.math.BigDecimal;

@Getter
public class CoPurchaseResponseV2 {

    private final Integer productId;
    private final String prodName;
    private final BigDecimal price;

    public CoPurchaseResponseV2(Product product) {
        this.productId = product.getProductId();
        this.prodName = product.getProdName();
        this.price = product.getPrice();
    }
}