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
@Table(name = "perceived_colour_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerceivedColourValue {

    @Id
    @Column(name = "perceived_colour_value_id")
    private Integer perceivedColourValueId;

    @Column(name = "perceived_colour_value_name")
    private String perceivedColourValueName;
}
