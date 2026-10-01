package com.winter.ecommerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
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

import com.winter.ecommerce.dto.OrderItemResponse;
import com.winter.ecommerce.dto.OrderRequest;
import com.winter.ecommerce.dto.OrderResponse;
import com.winter.ecommerce.entity.OrderStatus;
import com.winter.ecommerce.service.OrderNotCancellableException;
import com.winter.ecommerce.service.OrderService;
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
	private OrderService orderService;

	@InjectMocks
	private OrderController orderController;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(orderController)
				.setControllerAdvice(new ApiExceptionHandler())
				.build();
	}

	@Test
	void getsOrdersAndPassesOptionalFiltersToService() throws Exception {
		OrderResponse response = order(OrderStatus.PROCESSING);
		when(orderService.findOrders(OrderStatus.PROCESSING, 42)).thenReturn(List.of(response));

		mockMvc.perform(get("/api/orders")
						.param("status", "PROCESSING")
						.param("customerId", "42"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].customerId").value(42));

		verify(orderService).findOrders(OrderStatus.PROCESSING, 42);
	}

	@Test
	void listsOrdersWithoutFilters() throws Exception {
		when(orderService.findOrders(null, null)).thenReturn(List.of(order(OrderStatus.PENDING)));

		mockMvc.perform(get("/api/orders"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].status").value("PENDING"));

		verify(orderService).findOrders(null, null);
	}

	@Test
	void getsOrderOrReturnsNotFound() throws Exception {
		OrderResponse response = order(OrderStatus.PENDING);
		when(orderService.findOrder(response.id())).thenReturn(Optional.of(response));

		mockMvc.perform(get("/api/orders/{id}", response.id()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(response.id().toString()));

		when(orderService.findOrder(any(UUID.class))).thenReturn(Optional.empty());
		mockMvc.perform(get("/api/orders/{id}", UUID.randomUUID()))
				.andExpect(status().isNotFound());
	}

	@Test
	void cancelsOrderOrReturnsNotFound() throws Exception {
		OrderResponse response = order(OrderStatus.CANCELLED);
		when(orderService.cancelOrder(response.id())).thenReturn(Optional.of(response));

		mockMvc.perform(post("/api/orders/{id}/cancel", response.id()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"));

		when(orderService.cancelOrder(any(UUID.class))).thenReturn(Optional.empty());
		mockMvc.perform(post("/api/orders/{id}/cancel", UUID.randomUUID()))
				.andExpect(status().isNotFound());
	}

	@Test
	void returnsConflictWhenOrderCannotBeCancelled() throws Exception {
		when(orderService.cancelOrder(any(UUID.class))).thenThrow(new OrderNotCancellableException());

		mockMvc.perform(post("/api/orders/{id}/cancel", UUID.randomUUID()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Only pending orders can be cancelled."));
	}

	@Test
	void createsOrder() throws Exception {
		OrderResponse response = order(OrderStatus.PENDING);
		when(orderService.createOrder(any(OrderRequest.class))).thenReturn(response);
		String request = """
				{"customerId":42,"items":[{"productId":7,"productName":"Widget","quantity":2,"unitPrice":12.50}]}
				""";

		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(response.id().toString()))
				.andExpect(jsonPath("$.status").value("PENDING"));
	}

	@Test
	void returnsBadRequestForInvalidOrder() throws Exception {
		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"customerId":null,"items":[{"productId":7,"productName":"Widget","quantity":1,"unitPrice":1}]}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("customerId is required."));

		verifyNoInteractions(orderService);
	}

	@Test
	void returnsBadRequestForInvalidOrderItem() throws Exception {
		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"customerId":42,"items":[{"productId":7,"productName":"Widget","quantity":0,"unitPrice":1}]}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("quantity must be positive."));

		verifyNoInteractions(orderService);
	}

	@Test
	void updatesOrderOrReturnsNotFound() throws Exception {
		UUID id = UUID.randomUUID();
		OrderResponse response = order(OrderStatus.PROCESSING);
		when(orderService.updateOrder(any(UUID.class), any(OrderRequest.class))).thenReturn(Optional.of(response));
		String request = """
				{"customerId":42,"status":"PROCESSING","items":[{"productId":7,"productName":"Widget","quantity":1,"unitPrice":5.00}]}
				""";

		mockMvc.perform(put("/api/orders/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PROCESSING"));

		when(orderService.updateOrder(any(UUID.class), any(OrderRequest.class))).thenReturn(Optional.empty());
		mockMvc.perform(put("/api/orders/{id}", UUID.randomUUID())
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isNotFound());
	}

	@Test
	void deletesOrderOrReturnsNotFound() throws Exception {
		UUID id = UUID.randomUUID();
		when(orderService.deleteOrder(id)).thenReturn(true);

		mockMvc.perform(delete("/api/orders/{id}", id))
				.andExpect(status().isNoContent());

		when(orderService.deleteOrder(any(UUID.class))).thenReturn(false);
		mockMvc.perform(delete("/api/orders/{id}", UUID.randomUUID()))
				.andExpect(status().isNotFound());
	}

	private static OrderResponse order(OrderStatus status) {
		return new OrderResponse(
				UUID.randomUUID(),
				42,
				List.of(new OrderItemResponse(
						UUID.randomUUID(), 7, "Widget", 2,
						new BigDecimal("12.50"), new BigDecimal("25.00"))),
				new BigDecimal("25.00"),
				status,
				Instant.now(),
				Instant.now(),
				0L);
	}
}
