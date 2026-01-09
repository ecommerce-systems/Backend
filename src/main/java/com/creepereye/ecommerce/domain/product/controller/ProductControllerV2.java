package com.creepereye.ecommerce.domain.product.controller;

import com.creepereye.ecommerce.domain.product.entity.ProductSearch;
import com.creepereye.ecommerce.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/products")
@RequiredArgsConstructor
public class ProductControllerV2 {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductSearch> getProductById(@PathVariable Integer id) {
        // V2: Returns the denormalized entity directly (Fast, No Joins)
        return productService.findByIdV2(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<String>> searchProductNames(@RequestParam("keyword") String keyword,
                                                           @RequestParam(value = "productType", required = false) String productType,
                                                           @RequestParam(value = "department", required = false) String department,
                                                           @RequestParam(value = "productGroup", required = false) String productGroup,
                                                           @RequestParam(value = "section", required = false) String section) {
        
        // Use the multi-filter search method
        List<String> productNames = productService.searchNamesV2WithFilters(keyword, productType, department, productGroup, section);
        return ResponseEntity.ok(productNames);
    }
}