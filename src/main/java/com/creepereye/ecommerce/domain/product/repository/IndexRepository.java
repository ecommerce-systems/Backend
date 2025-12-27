package com.creepereye.ecommerce.domain.product.repository;

import com.creepereye.ecommerce.domain.product.entity.Index;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IndexRepository extends JpaRepository<Index, String> {
    Optional<Index> findByIndexName(String indexName);

    @Query("SELECT max(CAST(i.indexCode as int)) FROM Index i")
    Optional<Integer> findMaxIndexCodeAsInt();
}
