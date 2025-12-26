package com.creepereye.ecommerce.domain.product.repository;


import com.creepereye.ecommerce.domain.product.entity.GraphicalAppearance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GraphicalAppearanceRepository extends JpaRepository<GraphicalAppearance, Integer> {

    Optional<GraphicalAppearance> findByGraphicalAppearanceName(String graphicalAppearanceName);

    @Query("SELECT MAX(g.graphicalAppearanceNo) FROM GraphicalAppearance g")
    Optional<Integer> findMaxGraphicalAppearanceNo();
}
