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
@Table(name = "graphical_appearances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphicalAppearance {

    @Id
    @Column(name = "graphical_appearance_no")
    private Integer graphicalAppearanceNo;

    @Column(name = "graphical_appearance_name")
    private String graphicalAppearanceName;
}