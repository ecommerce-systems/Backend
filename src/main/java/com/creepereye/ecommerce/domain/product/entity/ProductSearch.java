package com.creepereye.ecommerce.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_searches", indexes = {
        @jakarta.persistence.Index(name = "idx_product_search_name", columnList = "prod_name"),
        @jakarta.persistence.Index(name = "idx_product_search_type", columnList = "product_type_name"),
        @jakarta.persistence.Index(name = "idx_product_search_dept", columnList = "department_name"),
        @jakarta.persistence.Index(name = "idx_product_search_group", columnList = "product_group_name"),
        @jakarta.persistence.Index(name = "idx_product_search_colour", columnList = "colour_group_name"),
        @jakarta.persistence.Index(name = "idx_product_search_section", columnList = "section_name"),
        @jakarta.persistence.Index(name = "idx_product_search_garment", columnList = "garment_group_name")
})
public class ProductSearch {

    @Id
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "prod_name")
    private String prodName;

    @Column(name = "detail_desc", columnDefinition = "TEXT")
    private String detailDesc;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "image_url")
    private String imageUrl;

    // Denormalized Category Names
    @Column(name = "product_type_name")
    private String productTypeName;

    @Column(name = "product_group_name")
    private String productGroupName;

    @Column(name = "graphical_appearance_name")
    private String graphicalAppearanceName;

    @Column(name = "colour_group_name")
    private String colourGroupName;

    @Column(name = "perceived_colour_value_name")
    private String perceivedColourValueName;

    @Column(name = "perceived_colour_master_name")
    private String perceivedColourMasterName;

    @Column(name = "department_name")
    private String departmentName;

    @Column(name = "index_name")
    private String indexName;

    @Column(name = "index_group_name")
    private String indexGroupName;

    @Column(name = "section_name")
    private String sectionName;

    @Column(name = "garment_group_name")
    private String garmentGroupName;
}
