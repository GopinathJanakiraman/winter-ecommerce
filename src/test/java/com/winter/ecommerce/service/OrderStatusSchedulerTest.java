package com.winter.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import com.winter.ecommerce.entity.Order;
import com.winter.ecommerce.entity.OrderStatus;
import com.winter.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderStatusSchedulerTest {

	@Mock
	private OrderRepository orderRepository;

	@Test
	void movesPendingOrdersToProcessing() {
		Order pending = new Order();
		pending.setStatus(OrderStatus.PENDING);
		when(orderRepository.findAllByStatus(OrderStatus.PENDING)).thenReturn(List.of(pending));
		OrderStatusScheduler scheduler = new OrderStatusScheduler(orderRepository);

		scheduler.processPendingOrders();

		assertEquals(OrderStatus.PROCESSING, pending.getStatus());
		assertNotNull(pending.getUpdatedAt());
		verify(orderRepository).saveAll(List.of(pending));
	}

	@Test
	void savesEmptyListWhenThereAreNoPendingOrders() {
		when(orderRepository.findAllByStatus(OrderStatus.PENDING)).thenReturn(List.of());

		new OrderStatusScheduler(orderRepository).processPendingOrders();

		verify(orderRepository).saveAll(List.of());
	}
}
