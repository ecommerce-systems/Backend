package com.creepereye.ecommerce.domain.product.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "garment_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GarmentGroup {

    @Id
    @Column(name = "garment_group_no")
    private Integer garmentGroupNo;

    @Column(name = "garment_group_name")
    private String garmentGroupName;
}

