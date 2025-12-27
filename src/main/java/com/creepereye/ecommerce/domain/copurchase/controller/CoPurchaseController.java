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