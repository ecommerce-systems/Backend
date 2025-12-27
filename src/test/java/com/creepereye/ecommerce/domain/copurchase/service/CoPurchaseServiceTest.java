package com.creepereye.ecommerce.domain.copurchase.service;

import com.creepereye.ecommerce.domain.order.entity.Order;
import com.creepereye.ecommerce.domain.order.entity.OrderDetail;
import com.creepereye.ecommerce.domain.order.repository.OrderRepository;
import com.creepereye.ecommerce.domain.product.entity.Product;
import com.creepereye.ecommerce.domain.copurchase.dto.CoPurchaseResponse;
import com.creepereye.ecommerce.domain.copurchase.entity.CoPurchase;
import com.creepereye.ecommerce.domain.copurchase.repository.CoPurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoPurchaseServiceTest {

    @Mock
    private CoPurchaseRepository coPurchaseRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CoPurchaseService coPurchaseService;

    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    void setUp() {
        product1 = Product.builder().productId(1).prodName("Product 1").build();
        product2 = Product.builder().productId(2).prodName("Product 2").build();
        product3 = Product.builder().productId(3).prodName("Product 3").build();
    }

    @Test
    void getRecommendations() {
        // given
        CoPurchase coPurchase = CoPurchase.builder().sourceProduct(product1).targetProduct(product2).score(10f).build();
        when(coPurchaseRepository.findBySourceProductProductIdOrderByScoreDesc(1))
                .thenReturn(Collections.singletonList(coPurchase));

        // when
        List<CoPurchaseResponse> recommendations = coPurchaseService.getRecommendations(1);

        // then
        assertThat(recommendations).hasSize(1);
        assertThat(recommendations.get(0).getProductId()).isEqualTo(2);
    }

    @Test
    void populateCoPurchaseData() {
        // given
        OrderDetail orderDetail1 = OrderDetail.builder().product(product1).build();
        OrderDetail orderDetail2 = OrderDetail.builder().product(product2).build();
        OrderDetail orderDetail3 = OrderDetail.builder().product(product3).build();
        Order order1 = Order.builder().orderDetails(Arrays.asList(orderDetail1, orderDetail2)).build();
        Order order2 = Order.builder().orderDetails(Arrays.asList(orderDetail1, orderDetail3)).build();
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));

        // when
        coPurchaseService.populateCoPurchaseData();

        // then
        verify(coPurchaseRepository, times(1)).saveAll(anyList());
    }
}