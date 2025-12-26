package com.creepereye.ecommerce.domain.copurchase.controller;

import com.creepereye.ecommerce.domain.copurchase.dto.CoPurchaseResponse;
import com.creepereye.ecommerce.domain.copurchase.service.CoPurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/co-purchase")
@RequiredArgsConstructor
public class CoPurchaseController {

    private final CoPurchaseService coPurchaseService;

    @GetMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public List<CoPurchaseResponse> getRecommendations(@PathVariable Integer productId) {
        return coPurchaseService.getRecommendations(productId);
    }
}