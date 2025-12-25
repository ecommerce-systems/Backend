package com.creepereye.ecommerce.domain.order.repository;

import com.creepereye.ecommerce.domain.order.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
}
