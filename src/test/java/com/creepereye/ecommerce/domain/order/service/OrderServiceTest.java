package com.creepereye.ecommerce.domain.order.service;

import com.creepereye.ecommerce.domain.order.dto.OrderItemRequest;
import com.creepereye.ecommerce.domain.order.dto.OrderRequest;
import com.creepereye.ecommerce.domain.order.dto.OrderResponse;
import com.creepereye.ecommerce.domain.order.entity.Order;
import com.creepereye.ecommerce.domain.order.repository.OrderRepository;
import com.creepereye.ecommerce.domain.product.entity.Product;
import com.creepereye.ecommerce.domain.product.repository.ProductRepository;
import com.creepereye.ecommerce.domain.user.entity.User;
import com.creepereye.ecommerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    private User testUser;
    private Product testProduct;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).name("testuser").build();
        testProduct = Product.builder().productId(1).prodName("Test Product").price(BigDecimal.TEN).build();

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1);
        itemRequest.setQuantity(2);

        orderRequest = new OrderRequest();
        orderRequest.setItems(Collections.singletonList(itemRequest));

        // Mock SecurityContext
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createOrder_Success() {
        when(userRepository.findByAuthUsername("testuser")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse orderResponse = orderService.createOrder(orderRequest);

        assertNotNull(orderResponse);
        assertEquals(1, orderResponse.getOrderDetails().size());
        assertEquals("PENDING", orderResponse.getStatus());
        assertEquals(0, new BigDecimal("20").compareTo(orderResponse.getOrderDetails().get(0).getPrice()));

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_ProductNotFound() {
        when(userRepository.findByAuthUsername("testuser")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(orderRequest));

        verify(orderRepository, never()).save(any(Order.class));
    }
}
