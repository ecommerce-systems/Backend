package com.creepereye.ecommerce.domain.product.repository;

import com.creepereye.ecommerce.domain.product.entity.Index;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IndexRepository extends JpaRepository<Index, Character> {
    Optional<Index> findByIndexName(String indexName);

    @Query("SELECT i.indexCode FROM Index i ORDER BY i.indexCode DESC LIMIT 1")
    Optional<Character> findTopByOrderByIndexCodeDesc();
}
