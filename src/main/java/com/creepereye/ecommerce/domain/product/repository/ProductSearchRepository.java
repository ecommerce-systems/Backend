package com.creepereye.ecommerce.domain.product.repository;

import com.creepereye.ecommerce.domain.product.entity.ProductSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchRepository extends JpaRepository<ProductSearch, Integer> {

    // 1. Prefix Search (Fastest, Index Optimized) - "Item%"
    List<ProductSearch> findTop20ByProdNameStartingWithIgnoreCase(String keyword);

    // 2. Contains Search (Slower, Full Index Scan) - "%Item%"
    List<ProductSearch> findTop20ByProdNameContainingIgnoreCase(String keyword);

    // Optimized: Filter by Category (Product Type) + Keyword
    List<ProductSearch> findByProductTypeNameAndProdNameContainingIgnoreCase(String productTypeName, String keyword);

    // Dynamic filtering for multiple categories
    @Query("SELECT p FROM ProductSearch p WHERE " +
            "(:productType IS NULL OR p.productTypeName = :productType) AND " +
            "(:department IS NULL OR p.departmentName = :department) AND " +
            "(:productGroup IS NULL OR p.productGroupName = :productGroup) AND " +
            "(:section IS NULL OR p.sectionName = :section) AND " +
            "p.prodName LIKE %:keyword%")
    List<ProductSearch> searchByMultipleCategories(
            @Param("keyword") String keyword,
            @Param("productType") String productType,
            @Param("department") String department,
            @Param("productGroup") String productGroup,
            @Param("section") String section);

    org.springframework.data.domain.Page<ProductSearch> findByProdNameContainingIgnoreCase(String keyword, org.springframework.data.domain.Pageable pageable);
}