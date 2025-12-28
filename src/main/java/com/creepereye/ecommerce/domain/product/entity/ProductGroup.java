package com.creepereye.ecommerce.domain.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_groups")
public class ProductGroup {

    @Id
    @Column(name = "product_group_code")
    private Character productGroupCode;

    @Column(name = "product_group_name")
    private String productGroupName;
}
