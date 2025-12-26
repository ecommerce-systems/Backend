package com.creepereye.ecommerce.domain.product.repository;

import com.creepereye.ecommerce.domain.product.entity.PerceivedColourMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerceivedColourMasterRepository extends JpaRepository<PerceivedColourMaster, Integer> {

    Optional<PerceivedColourMaster> findByPerceivedColourMasterName(String perceivedColourMasterName);

    @Query("SELECT MAX(p.perceivedColourMasterId) FROM PerceivedColourMaster p")
    Optional<Integer> findMaxPerceivedColourMasterId();
}
