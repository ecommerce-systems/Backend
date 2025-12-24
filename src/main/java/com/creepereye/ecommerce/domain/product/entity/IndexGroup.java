package com.creepereye.ecommerce.domain.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "index_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndexGroup {
    @Id
    @Column(name = "index_group_no")
    private Integer indexGroupNo;

    @Column(name = "index_group_name")
    private String indexGroupName;
}