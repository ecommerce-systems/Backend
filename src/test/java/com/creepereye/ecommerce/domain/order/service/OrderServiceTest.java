package com.creepereye.ecommerce.domain.order.service;

import com.creepereye.ecommerce.domain.order.dto.OrderItemRequest;
import com.creepereye.ecommerce.domain.order.dto.OrderRequest;
import com.creepereye.ecommerce.domain.order.dto.OrderResponse;
import com.creepereye.ecommerce.domain.order.entity.Order;
import com.creepereye.ecommerce.domain.order.entity.OrderDetail;
import com.creepereye.ecommerce.domain.order.repository.OrderRepository;
import com.creepereye.ecommerce.domain.product.entity.Product;
import com.creepereye.ecommerce.domain.product.repository.ProductRepository;
import com.creepereye.ecommerce.domain.auth.entity.Auth;
import com.creepereye.ecommerce.domain.user.entity.User;
import com.creepereye.ecommerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;

    @BeforeEach
    void setUp() {
        Auth auth = Auth.builder().username("testUser").build();
        user = User.builder().id(1L).auth(auth).build();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(userDetails.getUsername()).thenReturn("testUser");
        
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("주문 생성 시, OrderResponse가 정상적으로 반환된다")
    void createOrder_shouldCreateOrder() {
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1);
        itemRequest.setQuantity(2);
        OrderRequest request = new OrderRequest();
        request.setItems(Collections.singletonList(itemRequest));

        Product product = Product.builder().productId(1).price(BigDecimal.valueOf(100)).build();
        when(userRepository.findByAuthUsername("testUser")).thenReturn(Optional.of(user));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.createOrder(request);

        assertThat(response).isNotNull();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("내 주문 목록 조회 시, 현재 사용자의 주문 목록이 반환된다")
    void getMyOrders_shouldReturnOrderList() {
        Order order = Order.builder().user(user).status("PENDING").build();
        OrderDetail detail = OrderDetail.builder()
                .product(Product.builder().productId(1).price(BigDecimal.TEN).build())
                .quantity(1)
                .price(BigDecimal.TEN)
                .build();
        order.addOrderDetail(detail);

        when(userRepository.findByAuthUsername("testUser")).thenReturn(Optional.of(user));
        when(orderRepository.findByUserId(user.getId())).thenReturn(Collections.singletonList(order));

        List<OrderResponse> results = orderService.getMyOrders();

        assertThat(results).hasSize(1);
        verify(orderRepository).findByUserId(user.getId());
    }

    @Test
    @DisplayName("주문 상세 조회 시, 본인 소유의 주문이면 정상 반환된다")
    void getOrderById_shouldReturnOrder() {
        Order order = Order.builder().user(user).status("PENDING").build();
        OrderDetail detail = OrderDetail.builder()
                .product(Product.builder().productId(1).price(BigDecimal.TEN).build())
                .quantity(1)
                .price(BigDecimal.TEN)
                .build();
        order.addOrderDetail(detail);

        when(userRepository.findByAuthUsername("testUser")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("주문 상세 조회 시, 타인 소유의 주문이면 SecurityException이 발생한다")
    void getOrderById_shouldThrowSecurityException_whenNotOwner() {
        User otherUser = User.builder().id(2L).build();
        Order order = Order.builder().user(otherUser).build();

        when(userRepository.findByAuthUsername("testUser")).thenReturn(Optional.of(user));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderById(1L))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("not authorized");
    }
}
