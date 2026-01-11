package com.creepereye.ecommerce.domain.copurchase.controller;

import com.creepereye.ecommerce.domain.copurchase.dto.CoPurchaseCreateRequest;
import com.creepereye.ecommerce.domain.copurchase.dto.CoPurchaseResponse;
import com.creepereye.ecommerce.domain.copurchase.service.CoPurchaseServiceV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/co-purchase")
@RequiredArgsConstructor
public class CoPurchaseControllerV1 {

    private final CoPurchaseServiceV1 coPurchaseService;

    @GetMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public List<CoPurchaseResponse> getRecommendations(@PathVariable Integer productId) {
        return coPurchaseService.getRecommendations(productId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createCoPurchase(@RequestBody @Valid CoPurchaseCreateRequest request) {
        coPurchaseService.createCoPurchase(request);
        return ResponseEntity.ok().build();
    }
}