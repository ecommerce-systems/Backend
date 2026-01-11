package com.creepereye.ecommerce.domain.copurchase.controller;

import com.creepereye.ecommerce.domain.copurchase.dto.CoPurchaseResponse;
import com.creepereye.ecommerce.domain.copurchase.service.CoPurchaseServiceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/co-purchase")
@RequiredArgsConstructor
public class CoPurchaseControllerV2 {

    private final CoPurchaseServiceV2 coPurchaseService;

    @GetMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public List<CoPurchaseResponse> getRecommendations(@PathVariable Integer productId) {
        return coPurchaseService.getRecommendations(productId);
    }
}
