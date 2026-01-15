package com.creepereye.ecommerce.domain.copurchase.controller;

import com.creepereye.ecommerce.domain.copurchase.service.CoPurchaseService;
import com.creepereye.ecommerce.domain.product.entity.ProductSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v2/co-purchase")
@RequiredArgsConstructor
public class CoPurchaseControllerV2 {

    private final CoPurchaseService coPurchaseService;

    @GetMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public List<ProductSearch> getRecommendations(@PathVariable Integer productId) {
        return coPurchaseService.getRecommendationsV2(productId);
    }
}
