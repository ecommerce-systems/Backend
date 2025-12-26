package com.creepereye.ecommerce.domain.product.repository;


import com.creepereye.ecommerce.domain.product.entity.GarmentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GarmentGroupRepository extends JpaRepository<GarmentGroup, Integer> {

    Optional<GarmentGroup> findByGarmentGroupName(String garmentGroupName);

    @Query("SELECT MAX(g.garmentGroupNo) FROM GarmentGroup g")
    Optional<Integer> findMaxGarmentGroupNo();
}
