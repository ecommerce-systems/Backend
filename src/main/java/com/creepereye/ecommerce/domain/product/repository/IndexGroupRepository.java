package com.creepereye.ecommerce.domain.product.repository;


import com.creepereye.ecommerce.domain.product.entity.IndexGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IndexGroupRepository extends JpaRepository<IndexGroup, Integer> {

    Optional<IndexGroup> findByIndexGroupName(String indexGroupName);

    @Query("SELECT MAX(i.indexGroupNo) FROM IndexGroup i")
    Optional<Integer> findMaxIndexGroupNo();
}
