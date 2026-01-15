package com.creepereye.ecommerce.domain.copurchase.controller;

import com.creepereye.ecommerce.domain.copurchase.dto.CoPurchaseCreateRequest;
import com.creepereye.ecommerce.domain.copurchase.dto.CoPurchaseResponse;
import com.creepereye.ecommerce.domain.copurchase.service.CoPurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.creepereye.ecommerce.domain.copurchase.dto.CoPurchaseResponseV1;
import com.creepereye.ecommerce.domain.product.entity.ProductSearch;

@RestController
@RequiredArgsConstructor
public class CoPurchaseController {

    private final CoPurchaseService coPurchaseService;

    // V1: Returns only IDs (simulating need for 2nd request)
    @GetMapping("/api/v1/co-purchase/{productId}")
    @PreAuthorize("isAuthenticated()")
    public List<CoPurchaseResponseV1> getRecommendationsV1(@PathVariable Integer productId) {
        return coPurchaseService.getRecommendationsV1(productId);
    }

    // V2: Returns full metadata (denormalized)
    @GetMapping("/api/v2/co-purchase/{productId}")
    @PreAuthorize("isAuthenticated()")
    public List<ProductSearch> getRecommendationsV2(@PathVariable Integer productId) {
        return coPurchaseService.getRecommendationsV2(productId);
    }
}