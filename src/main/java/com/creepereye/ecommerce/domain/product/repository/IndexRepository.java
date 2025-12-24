package com.creepereye.ecommerce.domain.product.repository;

import com.creepereye.ecommerce.domain.product.entity.Index;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexRepository extends JpaRepository<Index, String> {
}
