package com.creepereye.ecommerce.domain.copurchase.service;

import com.creepereye.ecommerce.domain.order.entity.Order;
import com.creepereye.ecommerce.domain.order.entity.OrderDetail;
import com.creepereye.ecommerce.domain.order.repository.OrderRepository;
import com.creepereye.ecommerce.domain.product.entity.Product;
import com.creepereye.ecommerce.domain.copurchase.dto.CoPurchaseResponse;
import com.creepereye.ecommerce.domain.copurchase.entity.CoPurchase;
import com.creepereye.ecommerce.domain.copurchase.repository.CoPurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoPurchaseService {

    private final CoPurchaseRepository coPurchaseRepository;
    private final OrderRepository orderRepository;

    @Cacheable(value = "recommendations", key = "#productId")
    @Transactional(readOnly = true)
    public List<CoPurchaseResponse> getRecommendations(Integer productId) {
        List<CoPurchase> coPurchases = coPurchaseRepository.findBySourceProductProductIdOrderByCoPurchaseCountDesc(
                productId, PageRequest.of(0, 10));

        return coPurchases.stream()
                .map(coPurchase -> new CoPurchaseResponse(coPurchase.getTargetProduct()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void populateCoPurchaseData() {
        List<Order> orders = orderRepository.findAll();
        Map<Product, Map<Product, Long>> coPurchaseMap = new HashMap<>();

        for (Order order : orders) {
            List<OrderDetail> orderDetails = new ArrayList<>(order.getOrderDetails());
            for (int i = 0; i < orderDetails.size(); i++) {
                for (int j = i + 1; j < orderDetails.size(); j++) {
                    Product sourceProduct = orderDetails.get(i).getProduct();
                    Product targetProduct = orderDetails.get(j).getProduct();

                    coPurchaseMap.computeIfAbsent(sourceProduct, k -> new HashMap<>())
                            .merge(targetProduct, 1L, Long::sum);
                    coPurchaseMap.computeIfAbsent(targetProduct, k -> new HashMap<>())
                            .merge(sourceProduct, 1L, Long::sum);
                }
            }
        }

        List<CoPurchase> coPurchases = new ArrayList<>();
        for (Map.Entry<Product, Map<Product, Long>> entry : coPurchaseMap.entrySet()) {
            Product sourceProduct = entry.getKey();
            for (Map.Entry<Product, Long> innerEntry : entry.getValue().entrySet()) {
                Product targetProduct = innerEntry.getKey();
                Long count = innerEntry.getValue();
                coPurchases.add(CoPurchase.builder()
                        .sourceProduct(sourceProduct)
                        .targetProduct(targetProduct)
                        .coPurchaseCount(count)
                        .build());
            }
        }

        coPurchaseRepository.saveAll(coPurchases);
    }
}