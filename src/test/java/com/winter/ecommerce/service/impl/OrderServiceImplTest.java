package com.winter.ecommerce.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.winter.ecommerce.dto.OrderItemRequest;
import com.winter.ecommerce.dto.OrderRequest;
import com.winter.ecommerce.entity.Order;
import com.winter.ecommerce.entity.OrderStatus;
import com.winter.ecommerce.repository.OrderRepository;
import com.winter.ecommerce.service.OrderNotCancellableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

	@Mock
	private OrderRepository orderRepository;

	@Test
	void createsOrderWithCalculatedSubtotalsAndDefaultStatus() {
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
		OrderServiceImpl service = new OrderServiceImpl(orderRepository);

		var created = service.createOrder(new OrderRequest(
				42, null, List.of(new OrderItemRequest(7, "Widget", 2, new BigDecimal("12.50")))));

		assertEquals(OrderStatus.PENDING, created.status());
		assertEquals(new BigDecimal("25.00"), created.totalAmount());
		assertEquals(new BigDecimal("25.00"), created.items().get(0).subtotal());
	}

	@Test
	void cancelsPendingOrderAndRejectsOtherStatuses() {
		Order pending = order(OrderStatus.PENDING);
		when(orderRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
		when(orderRepository.save(pending)).thenReturn(pending);
		OrderServiceImpl service = new OrderServiceImpl(orderRepository);

		var cancelled = service.cancelOrder(pending.getId());

		assertEquals(OrderStatus.CANCELLED, cancelled.orElseThrow().status());
		assertTrue(cancelled.orElseThrow().updatedAt().compareTo(Instant.EPOCH) > 0);

		Order processing = order(OrderStatus.PROCESSING);
		when(orderRepository.findById(processing.getId())).thenReturn(Optional.of(processing));
		assertThrows(OrderNotCancellableException.class, () -> service.cancelOrder(processing.getId()));
	}

	@Test
	void movesPendingOrdersToProcessing() {
		Order pending = order(OrderStatus.PENDING);
		when(orderRepository.findAllByStatus(OrderStatus.PENDING)).thenReturn(List.of(pending));
		OrderServiceImpl service = new OrderServiceImpl(orderRepository);

		service.processPendingOrders();

		assertEquals(OrderStatus.PROCESSING, pending.getStatus());
		verify(orderRepository).saveAll(List.of(pending));
	}

	private static Order order(OrderStatus status) {
		Order order = new Order();
		order.setId(UUID.randomUUID());
		order.setCustomerId(42);
		order.setStatus(status);
		order.setCreatedAt(Instant.now());
		order.setUpdatedAt(Instant.now());
		order.setTotalAmount(BigDecimal.ZERO);
		order.setVersion(0L);
		return order;
	}
}
