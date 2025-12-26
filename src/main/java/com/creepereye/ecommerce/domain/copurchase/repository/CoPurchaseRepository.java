package com.creepereye.ecommerce.domain.copurchase.repository;

import com.creepereye.ecommerce.domain.copurchase.entity.CoPurchase;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoPurchaseRepository extends JpaRepository<CoPurchase, Long> {

    List<CoPurchase> findBySourceProductProductIdOrderByCoPurchaseCountDesc(Integer sourceProductId, Pageable pageable);
}