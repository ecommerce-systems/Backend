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
@Table(name = "perceived_colour_masters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerceivedColourMaster {

    @Id
    @Column(name = "perceived_colour_master_id")
    private Integer perceivedColourMasterId;

    @Column(name = "perceived_colour_master_name")
    private String perceivedColourMasterName;
}