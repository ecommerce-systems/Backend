package com.creepereye.ecommerce.domain.product.repository;

import com.creepereye.ecommerce.domain.product.entity.PerceivedColourMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerceivedColourMasterRepository extends JpaRepository<PerceivedColourMaster, Integer> {
}
