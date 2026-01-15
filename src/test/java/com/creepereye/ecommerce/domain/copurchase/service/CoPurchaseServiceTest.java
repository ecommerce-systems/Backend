package com.creepereye.ecommerce.domain.copurchase.service;

import com.creepereye.ecommerce.domain.copurchase.dto.CoPurchaseCreateRequest;
import com.creepereye.ecommerce.domain.copurchase.dto.CoPurchaseResponseV1;
import com.creepereye.ecommerce.domain.copurchase.entity.CoPurchase;
import com.creepereye.ecommerce.domain.copurchase.repository.CoPurchaseRepository;
import com.creepereye.ecommerce.domain.order.entity.Order;
import com.creepereye.ecommerce.domain.order.entity.OrderDetail;
import com.creepereye.ecommerce.domain.order.repository.OrderRepository;
import com.creepereye.ecommerce.domain.product.entity.Product;
import com.creepereye.ecommerce.domain.product.entity.ProductSearch;
import com.creepereye.ecommerce.domain.product.repository.ProductRepository;
import com.creepereye.ecommerce.domain.product.repository.ProductSearchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoPurchaseServiceTest {

    @Mock
    private CoPurchaseRepository coPurchaseRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductSearchRepository productSearchRepository;

    @InjectMocks
    private CoPurchaseService coPurchaseService;

    @Test
    @DisplayName("V1 - 추천 상품 ID 목록을 반환해야 한다")
    void getRecommendationsV1_shouldReturnListOfIds() {
        int productId = 1;
        Product sourceProduct = Product.builder().productId(productId).build();
        Product targetProduct = Product.builder().productId(2).build();
        CoPurchase coPurchase = CoPurchase.builder().sourceProduct(sourceProduct).targetProduct(targetProduct).build();

        when(coPurchaseRepository.findBySourceProductProductIdOrderByScoreDesc(productId))
                .thenReturn(Collections.singletonList(coPurchase));

        List<CoPurchaseResponseV1> result = coPurchaseService.getRecommendationsV1(productId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(2);
        verify(coPurchaseRepository).findBySourceProductProductIdOrderByScoreDesc(productId);
    }

    @Test
    @DisplayName("V2 - 추천 상품 상세 정보(ProductSearch) 목록을 반환해야 한다")
    void getRecommendationsV2_shouldReturnListOfProductSearch() {
        int productId = 1;
        Product sourceProduct = Product.builder().productId(productId).build();
        Product targetProduct = Product.builder().productId(2).build();
        CoPurchase coPurchase = CoPurchase.builder().sourceProduct(sourceProduct).targetProduct(targetProduct).build();

        ProductSearch recommendedProduct = ProductSearch.builder().productId(2).prodName("Recommended Product").build();

        when(coPurchaseRepository.findBySourceProductProductIdOrderByScoreDesc(productId))
                .thenReturn(Collections.singletonList(coPurchase));
        when(productSearchRepository.findAllById(anyList()))
                .thenReturn(Collections.singletonList(recommendedProduct));

        List<ProductSearch> result = coPurchaseService.getRecommendationsV2(productId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(2);
        assertThat(result.get(0).getProdName()).isEqualTo("Recommended Product");
        verify(coPurchaseRepository).findBySourceProductProductIdOrderByScoreDesc(productId);
        verify(productSearchRepository).findAllById(Collections.singletonList(2));
    }
    
    @Test
    @DisplayName("공동 구매 데이터 생성 시 정상적으로 저장되어야 한다")
    void createCoPurchase_shouldSaveCoPurchase() {
        CoPurchaseCreateRequest request = new CoPurchaseCreateRequest(1L, 2L, 5.0);
        Product source = Product.builder().productId(1).build();
        Product target = Product.builder().productId(2).build();

        when(productRepository.findById(1)).thenReturn(Optional.of(source));
        when(productRepository.findById(2)).thenReturn(Optional.of(target));

        coPurchaseService.createCoPurchase(request);

        verify(coPurchaseRepository).save(any(CoPurchase.class));
    }

    @Test
    @DisplayName("데이터 채우기 실행 시 주문 기반으로 공동 구매 관계가 저장되어야 한다")
    void populateCoPurchaseData_shouldProcessOrdersAndSave() {
        Product p1 = Product.builder().productId(1).build();
        Product p2 = Product.builder().productId(2).build();
        
        OrderDetail od1 = OrderDetail.builder().product(p1).build();
        OrderDetail od2 = OrderDetail.builder().product(p2).build();

        Order order = Order.builder().build();
        order.setOrderDetails(Arrays.asList(od1, od2));

        when(orderRepository.findAll()).thenReturn(Collections.singletonList(order));

        coPurchaseService.populateCoPurchaseData();

        verify(coPurchaseRepository).saveAll(anyList());
    }
}