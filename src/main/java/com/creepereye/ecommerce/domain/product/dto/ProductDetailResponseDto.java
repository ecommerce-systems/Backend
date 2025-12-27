package com.creepereye.ecommerce.domain.product.dto;

import com.creepereye.ecommerce.domain.product.entity.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ProductDetailResponseDto {
    private Integer productId;
    private String prodName;
    private String detailDesc;
    private BigDecimal price;
    private String imageUrl;
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

    public ProductDetailResponseDto(Product product, String imageUrl) {
        this.productId = product.getProductId();
        this.prodName = product.getProdName();
        this.detailDesc = product.getDetailDesc();
        this.price = product.getPrice();
        this.imageUrl = imageUrl;
        this.productTypeName = product.getProductType() != null ? product.getProductType().getProductTypeName() : null;
        this.productGroupName = product.getProductGroup() != null ? product.getProductGroup().getProductGroupName() : null;
        this.graphicalAppearanceName = product.getGraphicalAppearance() != null ? product.getGraphicalAppearance().getGraphicalAppearanceName() : null;
        this.colourGroupName = product.getColourGroup() != null ? product.getColourGroup().getColourGroupName() : null;
        this.perceivedColourValueName = product.getPerceivedColourValue() != null ? product.getPerceivedColourValue().getPerceivedColourValueName() : null;
        this.perceivedColourMasterName = product.getPerceivedColourMaster() != null ? product.getPerceivedColourMaster().getPerceivedColourMasterName() : null;
        this.departmentName = product.getDepartment() != null ? product.getDepartment().getDepartmentName() : null;
        this.indexName = product.getIndex() != null ? product.getIndex().getIndexName() : null;
        this.indexGroupName = product.getIndexGroup() != null ? product.getIndexGroup().getIndexGroupName() : null;
        this.sectionName = product.getSection() != null ? product.getSection().getSectionName() : null;
        this.garmentGroupName = product.getGarmentGroup() != null ? product.getGarmentGroup().getGarmentGroupName() : null;
    }
}