package com.winter.ecommerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.winter.ecommerce.entity.Order;
import com.winter.ecommerce.entity.OrderStatus;
import com.winter.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private OrderController orderController;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
	}

	@Test
	void getsOrdersWithoutFilters() throws Exception {
		when(orderRepository.findAll()).thenReturn(List.of(order(OrderStatus.PENDING)));

		mockMvc.perform(get("/api/orders"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].status").value("PENDING"));

		verify(orderRepository).findAll();
	}

	@Test
	void filtersOrdersByStatusAndCustomer() throws Exception {
		when(orderRepository.findAllByStatusAndCustomerId(OrderStatus.PROCESSING, 42))
				.thenReturn(List.of(order(OrderStatus.PROCESSING)));

		mockMvc.perform(get("/api/orders")
						.param("status", "PROCESSING")
						.param("customerId", "42"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].customerId").value(42));

		verify(orderRepository).findAllByStatusAndCustomerId(OrderStatus.PROCESSING, 42);
	}

	@Test
	void filtersOrdersByStatusOnly() throws Exception {
		when(orderRepository.findAllByStatus(OrderStatus.PENDING))
				.thenReturn(List.of(order(OrderStatus.PENDING)));

		mockMvc.perform(get("/api/orders").param("status", "PENDING"))
				.andExpect(status().isOk());

		verify(orderRepository).findAllByStatus(OrderStatus.PENDING);
	}

	@Test
	void filtersOrdersByCustomerOnly() throws Exception {
		when(orderRepository.findAllByCustomerId(42)).thenReturn(List.of(order(OrderStatus.PENDING)));

		mockMvc.perform(get("/api/orders").param("customerId", "42"))
				.andExpect(status().isOk());

		verify(orderRepository).findAllByCustomerId(42);
	}

	@Test
	void getsOrderAndReturnsNotFoundWhenMissing() throws Exception {
		Order found = order(OrderStatus.PENDING);
		when(orderRepository.findById(found.getId())).thenReturn(Optional.of(found));

		mockMvc.perform(get("/api/orders/{id}", found.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(found.getId().toString()));

		when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
		mockMvc.perform(get("/api/orders/{id}", UUID.randomUUID()))
				.andExpect(status().isNotFound());
	}

	@Test
	void cancelsPendingOrder() throws Exception {
		Order pending = order(OrderStatus.PENDING);
		when(orderRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
		when(orderRepository.save(pending)).thenReturn(pending);

		mockMvc.perform(post("/api/orders/{id}/cancel", pending.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"));
	}

	@Test
	void cannotCancelNonPendingOrder() throws Exception {
		Order processing = order(OrderStatus.PROCESSING);
		when(orderRepository.findById(processing.getId())).thenReturn(Optional.of(processing));

		mockMvc.perform(post("/api/orders/{id}/cancel", processing.getId()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Only pending orders can be cancelled."));

		when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
		mockMvc.perform(post("/api/orders/{id}/cancel", UUID.randomUUID()))
				.andExpect(status().isNotFound());
	}

	@Test
	void createsOrderWithCalculatedItemTotals() throws Exception {
		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
		String request = """
				{"customerId":42,"items":[{"productId":7,"productName":"Widget","quantity":2,"unitPrice":12.50}]}
				""";

		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andExpect(jsonPath("$.totalAmount").value(25.0))
				.andExpect(jsonPath("$.items[0].subtotal").value(25.0));
	}

	@Test
	void rejectsInvalidOrderRequests() throws Exception {
		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"items\":[]}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("customerId is required."));

		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"customerId\":42,\"items\":[]}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("At least one order item is required."));

		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"customerId\":42,\"items\":[{\"productId\":7,\"productName\":\"Widget\",\"quantity\":0,\"unitPrice\":1}]}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void updatesOrderAndReturnsNotFoundWhenMissing() throws Exception {
		Order existing = order(OrderStatus.PENDING);
		when(orderRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
		when(orderRepository.save(existing)).thenReturn(existing);
		String request = """
				{"customerId":42,"status":"PROCESSING","items":[{"productId":7,"productName":"Widget","quantity":1,"unitPrice":5.00}]}
				""";

		mockMvc.perform(put("/api/orders/{id}", existing.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PROCESSING"));

		when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
		mockMvc.perform(put("/api/orders/{id}", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isNotFound());
	}

	@Test
	void deletesOrderOrReturnsNotFound() throws Exception {
		UUID id = UUID.randomUUID();
		when(orderRepository.existsById(id)).thenReturn(true);

		mockMvc.perform(delete("/api/orders/{id}", id))
				.andExpect(status().isNoContent());

		when(orderRepository.existsById(any(UUID.class))).thenReturn(false);
		mockMvc.perform(delete("/api/orders/{id}", UUID.randomUUID()))
				.andExpect(status().isNotFound());
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
