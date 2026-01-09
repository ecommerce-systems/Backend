package com.creepereye.ecommerce.domain.product.controller;

import com.creepereye.ecommerce.domain.product.entity.*;
import com.creepereye.ecommerce.domain.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<String>> getAllColourGroups() {
        return ResponseEntity.ok(categoryService.findAllColourGroups().stream()
                .map(ColourGroup::getColourGroupName)
                .toList());
    }

    @GetMapping("/departments")
    public ResponseEntity<List<String>> getAllDepartments() {
        return ResponseEntity.ok(categoryService.findAllDepartments().stream()
                .map(Department::getDepartmentName)
                .toList());
    }

    @GetMapping("/garment-groups")
    public ResponseEntity<List<String>> getAllGarmentGroups() {
        return ResponseEntity.ok(categoryService.findAllGarmentGroups().stream()
                .map(GarmentGroup::getGarmentGroupName)
                .toList());
    }

    @GetMapping("/graphical-appearances")
    public ResponseEntity<List<String>> getAllGraphicalAppearances() {
        return ResponseEntity.ok(categoryService.findAllGraphicalAppearances().stream()
                .map(GraphicalAppearance::getGraphicalAppearanceName)
                .toList());
    }

    @GetMapping("/index-groups")
    public ResponseEntity<List<String>> getAllIndexGroups() {
        return ResponseEntity.ok(categoryService.findAllIndexGroups().stream()
                .map(IndexGroup::getIndexGroupName)
                .toList());
    }

    @GetMapping("/indices")
    public ResponseEntity<List<String>> getAllIndices() {
        return ResponseEntity.ok(categoryService.findAllIndices().stream()
                .map(Index::getIndexName)
                .toList());
    }

    @GetMapping("/perceived-colour-masters")
    public ResponseEntity<List<String>> getAllPerceivedColourMasters() {
        return ResponseEntity.ok(categoryService.findAllPerceivedColourMasters().stream()
                .map(PerceivedColourMaster::getPerceivedColourMasterName)
                .toList());
    }

    @GetMapping("/perceived-colour-values")
    public ResponseEntity<List<String>> getAllPerceivedColourValues() {
        return ResponseEntity.ok(categoryService.findAllPerceivedColourValues().stream()
                .map(PerceivedColourValue::getPerceivedColourValueName)
                .toList());
    }

    @GetMapping("/product-groups")
    public ResponseEntity<List<String>> getAllProductGroups() {
        return ResponseEntity.ok(categoryService.findAllProductGroups().stream()
                .map(ProductGroup::getProductGroupName)
                .toList());
    }

    @GetMapping("/product-types")
    public ResponseEntity<List<String>> getAllProductTypes() {
        return ResponseEntity.ok(categoryService.findAllProductTypes().stream()
                .map(ProductType::getProductTypeName)
                .toList());
    }

    @GetMapping("/sections")
    public ResponseEntity<List<String>> getAllSections() {
        return ResponseEntity.ok(categoryService.findAllSections().stream()
                .map(Section::getSectionName)
                .toList());
    }
}