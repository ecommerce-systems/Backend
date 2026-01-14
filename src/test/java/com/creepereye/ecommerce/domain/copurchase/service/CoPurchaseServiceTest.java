package com.creepereye.ecommerce.domain.copurchase.service;

import com.creepereye.ecommerce.domain.copurchase.dto.CoPurchaseCreateRequest;
import com.creepereye.ecommerce.domain.copurchase.dto.CoPurchaseResponse;
import com.creepereye.ecommerce.domain.copurchase.entity.CoPurchase;
import com.creepereye.ecommerce.domain.copurchase.repository.CoPurchaseRepository;
import com.creepereye.ecommerce.domain.order.entity.Order;
import com.creepereye.ecommerce.domain.order.entity.OrderDetail;
import com.creepereye.ecommerce.domain.order.repository.OrderRepository;
import com.creepereye.ecommerce.domain.product.entity.Product;
import com.creepereye.ecommerce.domain.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoPurchaseServiceTest {

    @Mock
    private CoPurchaseRepository coPurchaseRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CoPurchaseService coPurchaseService;

    @Test
    @DisplayName("getRecommendations should return list of CoPurchaseResponse")
    void getRecommendations_shouldReturnResponseList() {
        int productId = 1;
        Product targetProduct = Product.builder().productId(2).prodName("Target").build();
        CoPurchase coPurchase = CoPurchase.builder()
                .sourceProduct(Product.builder().productId(productId).build())
                .targetProduct(targetProduct)
                .score(10.0f)
                .build();

        when(coPurchaseRepository.findBySourceProductProductIdOrderByScoreDesc(productId))
                .thenReturn(Collections.singletonList(coPurchase));

        List<CoPurchaseResponse> result = coPurchaseService.getRecommendations(productId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProdName()).isEqualTo("Target");
        verify(coPurchaseRepository).findBySourceProductProductIdOrderByScoreDesc(productId);
    }

    @Test
    @DisplayName("createCoPurchase should save CoPurchase")
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
    @DisplayName("createCoPurchase should throw exception when product not found")
    void createCoPurchase_shouldThrowException_whenProductNotFound() {
        CoPurchaseCreateRequest request = new CoPurchaseCreateRequest(1L, 2L, 5.0);

        when(productRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> coPurchaseService.createCoPurchase(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Source Product not found");
    }

    @Test
    @DisplayName("populateCoPurchaseData should process orders and save CoPurchases")
    void populateCoPurchaseData_shouldProcessOrders() {
        Product p1 = Product.builder().productId(1).prodName("P1").build();
        Product p2 = Product.builder().productId(2).prodName("P2").build();
        Product p3 = Product.builder().productId(3).prodName("P3").build();

        OrderDetail d1 = OrderDetail.builder().product(p1).build();
        OrderDetail d2 = OrderDetail.builder().product(p2).build();
        OrderDetail d3 = OrderDetail.builder().product(p3).build();

        Order o1 = Order.builder().build();
        List<OrderDetail> details1 = new ArrayList<>();
        details1.add(d1);
        details1.add(d2);
        o1.setOrderDetails(details1);

        Order o2 = Order.builder().build();
        List<OrderDetail> details2 = new ArrayList<>();
        details2.add(d1);
        details2.add(d3);
        o2.setOrderDetails(details2);

        when(orderRepository.findAll()).thenReturn(Arrays.asList(o1, o2));

        coPurchaseService.populateCoPurchaseData();

        verify(coPurchaseRepository).saveAll(anyList());
    }
}