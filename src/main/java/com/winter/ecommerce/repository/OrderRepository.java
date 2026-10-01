package com.winter.ecommerce.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.winter.ecommerce.entity.Order;
import com.winter.ecommerce.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, UUID> {
	List<Order> findAllByStatus(OrderStatus status);

	List<Order> findAllByCustomerId(Integer customerId);

	List<Order> findAllByStatusAndCustomerId(OrderStatus status, Integer customerId);
}
