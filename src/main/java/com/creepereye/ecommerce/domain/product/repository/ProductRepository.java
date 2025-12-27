package com.creepereye.ecommerce.domain.product.repository;


import com.creepereye.ecommerce.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;



public interface ProductRepository extends JpaRepository<Product, Integer> {



    Page<Product> findByProdNameContainingIgnoreCase(String keyword, Pageable pageable);



    List<Product> findByProdNameContainingIgnoreCase(String keyword);



    List<Product> findTop20ByProdNameContainingIgnoreCase(String keyword);

}
