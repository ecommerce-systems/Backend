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
@Table(name = "colour_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ColourGroup {

    @Id
    @Column(name = "colour_group_code")
    private Integer colourGroupCode;

    @Column(name = "colour_group_name")
    private String colourGroupName;
}