package com.creepereye.ecommerce.domain.product.repository;


import com.creepereye.ecommerce.domain.product.entity.ProductGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductGroupRepository extends JpaRepository<ProductGroup, String> {

    Optional<ProductGroup> findByProductGroupName(String productGroupName);

    @Query("SELECT max(CAST(p.productGroupCode as int)) FROM ProductGroup p")
    Optional<Integer> findMaxProductGroupCodeAsInt();
}