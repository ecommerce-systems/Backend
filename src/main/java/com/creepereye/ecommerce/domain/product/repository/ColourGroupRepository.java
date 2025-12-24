package com.creepereye.ecommerce.domain.product.repository;


import com.creepereye.ecommerce.domain.product.entity.ColourGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColourGroupRepository extends JpaRepository<ColourGroup, Integer> {
}
