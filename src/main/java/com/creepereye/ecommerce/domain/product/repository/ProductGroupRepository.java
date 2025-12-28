package com.creepereye.ecommerce.domain.product.repository;


import com.creepereye.ecommerce.domain.product.entity.ProductGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductGroupRepository extends JpaRepository<ProductGroup, Character> {

    Optional<ProductGroup> findByProductGroupName(String productGroupName);

    @Query("SELECT pg.productGroupCode FROM ProductGroup pg ORDER BY pg.productGroupCode DESC LIMIT 1")
    Optional<Character> findTopByOrderByProductGroupCodeDesc();
}