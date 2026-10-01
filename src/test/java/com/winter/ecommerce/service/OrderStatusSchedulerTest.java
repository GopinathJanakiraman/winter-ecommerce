package com.winter.ecommerce.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderStatusSchedulerTest {

	@Mock
	private OrderService orderService;

	@Test
	void delegatesPendingOrderProcessingToService() {
		OrderStatusScheduler scheduler = new OrderStatusScheduler(orderService);

		scheduler.processPendingOrders();

		verify(orderService).processPendingOrders();
	}
}
