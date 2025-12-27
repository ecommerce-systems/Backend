package com.creepereye.ecommerce.domain.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateRequestDto {
    private Integer productId;
    private Integer productCode;
    private String prodName;
    private String productTypeName;
    private String productGroupName;
    private String graphicalAppearanceName;
    private String colourGroupName;
    private String perceivedColourValueName;
    private String perceivedColourMasterName;
    private String departmentName;
    private String indexName;
    private String indexGroupName;
    private String sectionName;
    private String garmentGroupName;
    private String detailDesc;
    private BigDecimal price;
}
