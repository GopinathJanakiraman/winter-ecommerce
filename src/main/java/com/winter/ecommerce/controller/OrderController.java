package com.winter.ecommerce.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.winter.ecommerce.dto.OrderRequest;
import com.winter.ecommerce.dto.OrderResponse;
import com.winter.ecommerce.entity.OrderStatus;
import com.winter.ecommerce.service.OrderService;
import jakarta.validation.Valid;

/**
 * HTTP adapter for order operations; order rules and persistence live in OrderService.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping
	public List<OrderResponse> getOrders(
			@RequestParam(required = false) OrderStatus status,
			@RequestParam(required = false) Integer customerId) {
		return orderService.findOrders(status, customerId);
	}

	@GetMapping("/{id}")
	public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
		return orderService.findOrder(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping("/{id}/cancel")
	public ResponseEntity<?> cancelOrder(@PathVariable UUID id) {
		return orderService.cancelOrder(id)
				.<ResponseEntity<?>>map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
		OrderResponse response = orderService.createOrder(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<OrderResponse> updateOrder(
			@PathVariable UUID id,
			@Valid @RequestBody OrderRequest request) {
		return orderService.updateOrder(id, request)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
		return orderService.deleteOrder(id)
				? ResponseEntity.noContent().build()
				: ResponseEntity.notFound().build();
	}

}
