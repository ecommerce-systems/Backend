package com.creepereye.ecommerce.domain.product.controller;


import com.creepereye.ecommerce.domain.product.entity.*;
import com.creepereye.ecommerce.domain.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/colour-groups")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ColourGroup>> getAllColourGroups() {
        return ResponseEntity.ok(categoryService.findAllColourGroups());
    }

    @GetMapping("/departments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(categoryService.findAllDepartments());
    }

    @GetMapping("/garment-groups")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GarmentGroup>> getAllGarmentGroups() {
        return ResponseEntity.ok(categoryService.findAllGarmentGroups());
    }

    @GetMapping("/graphical-appearances")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GraphicalAppearance>> getAllGraphicalAppearances() {
        return ResponseEntity.ok(categoryService.findAllGraphicalAppearances());
    }

    @GetMapping("/index-groups")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<IndexGroup>> getAllIndexGroups() {
        return ResponseEntity.ok(categoryService.findAllIndexGroups());
    }

    @GetMapping("/indices")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Index>> getAllIndices() {
        return ResponseEntity.ok(categoryService.findAllIndices());
    }

    @GetMapping("/perceived-colour-masters")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PerceivedColourMaster>> getAllPerceivedColourMasters() {
        return ResponseEntity.ok(categoryService.findAllPerceivedColourMasters());
    }

    @GetMapping("/perceived-colour-values")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PerceivedColourValue>> getAllPerceivedColourValues() {
        return ResponseEntity.ok(categoryService.findAllPerceivedColourValues());
    }

    @GetMapping("/product-groups")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductGroup>> getAllProductGroups() {
        return ResponseEntity.ok(categoryService.findAllProductGroups());
    }

    @GetMapping("/product-types")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductType>> getAllProductTypes() {
        return ResponseEntity.ok(categoryService.findAllProductTypes());
    }

    @GetMapping("/sections")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Section>> getAllSections() {
        return ResponseEntity.ok(categoryService.findAllSections());
    }
}
