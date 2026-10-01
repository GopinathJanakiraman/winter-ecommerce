package com.winter.ecommerce.service;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.winter.ecommerce.entity.Order;
import com.winter.ecommerce.entity.OrderStatus;
import com.winter.ecommerce.repository.OrderRepository;

/**
 * Advances pending orders automatically while the application is running.
 */
@Component
public class OrderStatusScheduler {

	private final OrderRepository orderRepository;

	public OrderStatusScheduler(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	/**
	 * Moves every pending order to processing and records a common update time.
	 * Runs five minutes after startup and every five minutes thereafter.
	 */
	@Scheduled(fixedRate = 300_000, initialDelay = 300_000)
	@Transactional
	public void processPendingOrders() {
		List<Order> pendingOrders = orderRepository.findAllByStatus(OrderStatus.PENDING);
		Instant now = Instant.now();
		for (Order order : pendingOrders) {
			order.setStatus(OrderStatus.PROCESSING);
			order.setUpdatedAt(now);
		}
		orderRepository.saveAll(pendingOrders);
	}
}
