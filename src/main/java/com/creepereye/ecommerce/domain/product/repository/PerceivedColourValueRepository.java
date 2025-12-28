package com.creepereye.ecommerce.domain.product.repository;


import com.creepereye.ecommerce.domain.product.entity.PerceivedColourValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerceivedColourValueRepository extends JpaRepository<PerceivedColourValue, Integer> {

    Optional<PerceivedColourValue> findByPerceivedColourValueName(String perceivedColourValueName);

    @Query("SELECT MAX(p.perceivedColourValueId) FROM PerceivedColourValue p")
    Optional<Integer> findMaxPerceivedColourValueId();
}
