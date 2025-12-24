package com.creepereye.ecommerce.domain.product.repository;


import com.creepereye.ecommerce.domain.product.entity.PerceivedColourValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerceivedColourValueRepository extends JpaRepository<PerceivedColourValue, Integer> {
}
