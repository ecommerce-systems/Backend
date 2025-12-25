package com.creepereye.ecommerce.domain.product.entity;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.*;


@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_code")
    private Integer productCode;

    @Column(name = "prod_name")
    private String prodName;

    @Column(name = "detail_desc", length = 800)
    private String detailDesc;

    @ManyToOne
    @JoinColumn(name = "product_type_no")
    private ProductType productType;

    @ManyToOne
    @JoinColumn(name = "product_group_name")
    private ProductGroup productGroup;

    @ManyToOne
    @JoinColumn(name = "graphical_appearance_no")
    private GraphicalAppearance graphicalAppearance;

    @ManyToOne
    @JoinColumn(name = "colour_group_code")
    private ColourGroup colourGroup;

    @ManyToOne
    @JoinColumn(name = "perceived_colour_value_id")
    private PerceivedColourValue perceivedColourValue;

    @ManyToOne
    @JoinColumn(name = "perceived_colour_master_id")
    private PerceivedColourMaster perceivedColourMaster;

    @ManyToOne
    @JoinColumn(name = "department_no")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "index_code")
    private Index index;

    @ManyToOne
    @JoinColumn(name = "index_group_no")
    private IndexGroup indexGroup;

    @ManyToOne
    @JoinColumn(name = "section_no")
    private Section section;

    @ManyToOne
    @JoinColumn(name = "garment_group_no")
    private GarmentGroup garmentGroup;

    @Column(name = "price")
    private java.math.BigDecimal price;
}