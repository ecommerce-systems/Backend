package com.creepereye.ecommerce.domain.copurchase.service;

import com.creepereye.ecommerce.domain.copurchase.dto.CoPurchaseResponse;
import com.creepereye.ecommerce.domain.copurchase.entity.CoPurchase;
import com.creepereye.ecommerce.domain.copurchase.repository.CoPurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoPurchaseServiceV2 {

    private final CoPurchaseRepository coPurchaseRepository;

    @Cacheable(value = "recommendations", key = "#productId")
    @Transactional(readOnly = true)
    public List<CoPurchaseResponse> getRecommendations(Integer productId) {
        List<CoPurchase> coPurchases = coPurchaseRepository.findBySourceProductProductIdOrderByScoreDesc(productId);

        return coPurchases.stream()
                .map(coPurchase -> new CoPurchaseResponse(coPurchase.getTargetProduct()))
                .collect(Collectors.toList());
    }
}
