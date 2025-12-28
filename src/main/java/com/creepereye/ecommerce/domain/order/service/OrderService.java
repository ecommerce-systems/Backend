package com.creepereye.ecommerce.domain.order.service;

import com.creepereye.ecommerce.domain.order.dto.OrderRequest;
import com.creepereye.ecommerce.domain.order.dto.OrderResponse;
import com.creepereye.ecommerce.domain.order.entity.Order;
import com.creepereye.ecommerce.domain.order.entity.OrderDetail;
import com.creepereye.ecommerce.domain.order.repository.OrderRepository;
import com.creepereye.ecommerce.domain.product.entity.Product;
import com.creepereye.ecommerce.domain.product.repository.ProductRepository;
import com.creepereye.ecommerce.domain.user.entity.User;
import com.creepereye.ecommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        User user = getCurrentAuthenticatedUser();

        Order order = Order.builder()
                .user(user)
                .status("PENDING")
                .build();

        orderRequest.getItems().forEach(itemRequest -> {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + itemRequest.getProductId()));

            if (product.getPrice() == null) {
                throw new IllegalStateException("Product price is not set for product id: " + product.getProductId());
            }
            
            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())))
                    .build();
            order.addOrderDetail(orderDetail);
        });

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        User user = getCurrentAuthenticatedUser();
        List<Order> orders = orderRepository.findByUserId(user.getId());
        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        User user = getCurrentAuthenticatedUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));
        
        if (!order.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You are not authorized to view this order");
        }
        
        return OrderResponse.from(order);
    }

    private User getCurrentAuthenticatedUser() {
        String username;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return userRepository.findByAuthUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
