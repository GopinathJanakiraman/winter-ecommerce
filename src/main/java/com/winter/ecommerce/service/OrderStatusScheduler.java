package com.winter.ecommerce.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.winter.ecommerce.service.OrderService;

/**
 * Advances pending orders automatically while the application is running.
 */
@Component
public class OrderStatusScheduler {

	private final OrderService orderService;

	public OrderStatusScheduler(OrderService orderService) {
		this.orderService = orderService;
	}

	/**
	 * Moves every pending order to processing and records a common update time.
	 * Runs five minutes after startup and every five minutes thereafter.
	 */
	@Scheduled(fixedRate = 300_000, initialDelay = 300_000)
	public void processPendingOrders() {
		orderService.processPendingOrders();
	}
}
