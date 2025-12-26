package com.creepereye.ecommerce.domain.product.repository;


import com.creepereye.ecommerce.domain.product.entity.ColourGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ColourGroupRepository extends JpaRepository<ColourGroup, Integer> {

    Optional<ColourGroup> findByColourGroupName(String colourGroupName);

    @Query("SELECT MAX(c.colourGroupCode) FROM ColourGroup c")
    Optional<Integer> findMaxColourGroupCode();
}
