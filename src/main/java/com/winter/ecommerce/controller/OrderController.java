package com.winter.ecommerce.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.winter.ecommerce.entity.Order;
import com.winter.ecommerce.entity.OrderItem;
import com.winter.ecommerce.entity.OrderStatus;
import com.winter.ecommerce.repository.OrderRepository;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderRepository orderRepository;

	public OrderController(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	@GetMapping
	@Transactional(readOnly = true)
	public List<OrderResponse> getOrders(
			@RequestParam(required = false) OrderStatus status,
			@RequestParam(required = false) Integer customerId) {
		List<Order> orders;
		if (status != null && customerId != null) {
			orders = orderRepository.findAllByStatusAndCustomerId(status, customerId);
		} else if (status != null) {
			orders = orderRepository.findAllByStatus(status);
		} else if (customerId != null) {
			orders = orderRepository.findAllByCustomerId(customerId);
		} else {
			orders = orderRepository.findAll();
		}
		return orders.stream()
				.map(OrderController::toResponse)
				.toList();
	}

	@GetMapping("/{id}")
	@Transactional(readOnly = true)
	public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
		return orderRepository.findById(id)
				.map(order -> ResponseEntity.ok(toResponse(order)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping("/{id}/cancel")
	@Transactional
	public ResponseEntity<?> cancelOrder(@PathVariable UUID id) {
		return orderRepository.findById(id)
				.map(order -> {
					if (order.getStatus() != OrderStatus.PENDING) {
						return ResponseEntity.status(409)
								.body(new ErrorResponse("Only pending orders can be cancelled."));
					}
					order.setStatus(OrderStatus.CANCELLED);
					order.setUpdatedAt(Instant.now());
					return ResponseEntity.ok(toResponse(orderRepository.save(order)));
				})
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	@Transactional
	public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
		String validationError = validate(request);
		if (validationError != null) {
			return ResponseEntity.badRequest().body(new ErrorResponse(validationError));
		}

		Instant now = Instant.now();
		Order order = new Order();
		order.setId(UUID.randomUUID());
		order.setCustomerId(request.customerId());
		order.setStatus(request.status() == null ? OrderStatus.PENDING : request.status());
		order.setCreatedAt(now);
		order.setUpdatedAt(now);
		replaceItems(order, request.items());

		OrderResponse response = toResponse(orderRepository.save(order));
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@PutMapping("/{id}")
	@Transactional
	public ResponseEntity<?> updateOrder(
			@PathVariable UUID id,
			@RequestBody OrderRequest request) {
		String validationError = validate(request);
		if (validationError != null) {
			return ResponseEntity.badRequest().body(new ErrorResponse(validationError));
		}

		return orderRepository.findById(id)
				.map(order -> {
					order.setCustomerId(request.customerId());
					if (request.status() != null) {
						order.setStatus(request.status());
					}
					replaceItems(order, request.items());
					order.setUpdatedAt(Instant.now());
					return ResponseEntity.ok(toResponse(orderRepository.save(order)));
				})
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	@Transactional
	public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
		if (!orderRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		orderRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	private static void replaceItems(Order order, List<OrderItemRequest> requests) {
		order.getItems().clear();
		for (OrderItemRequest request : requests) {
			OrderItem item = new OrderItem();
			item.setId(UUID.randomUUID());
			item.setProductId(request.productId());
			item.setProductName(request.productName());
			item.setQuantity(request.quantity());
			item.setUnitPrice(request.unitPrice());
			item.setSubtotal(request.unitPrice().multiply(BigDecimal.valueOf(request.quantity())));
			item.setOrder(order);
			order.getItems().add(item);
		}
		order.setTotalAmount(order.getItems().stream()
				.map(OrderItem::getSubtotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add));
	}

	private static String validate(OrderRequest request) {
		if (request == null) {
			return "Order request is required.";
		}
		if (request.customerId() == null) {
			return "customerId is required.";
		}
		if (request.items() == null || request.items().isEmpty()) {
			return "At least one order item is required.";
		}
		for (int i = 0; i < request.items().size(); i++) {
			OrderItemRequest item = request.items().get(i);
			if (item == null
					|| item.productId() == null
					|| item.productName() == null || item.productName().isBlank()
					|| item.quantity() <= 0
					|| item.unitPrice() == null || item.unitPrice().signum() < 0) {
				return "Each item requires a productId, productName, positive quantity, and non-negative unitPrice (item "
						+ i + ").";
			}
		}
		return null;
	}

	private static OrderResponse toResponse(Order order) {
		List<OrderItemResponse> items = order.getItems().stream()
				.map(item -> new OrderItemResponse(
						item.getId(),
						item.getProductId(),
						item.getProductName(),
						item.getQuantity(),
						item.getUnitPrice(),
						item.getSubtotal()))
				.toList();
		return new OrderResponse(
				order.getId(),
				order.getCustomerId(),
				items,
				order.getTotalAmount(),
				order.getStatus(),
				order.getCreatedAt(),
				order.getUpdatedAt(),
				order.getVersion());
	}

	public record OrderRequest(Integer customerId, OrderStatus status, List<OrderItemRequest> items) {
	}

	public record OrderItemRequest(
			Integer productId,
			String productName,
			int quantity,
			BigDecimal unitPrice) {
	}

	public record OrderResponse(
			UUID id,
			Integer customerId,
			List<OrderItemResponse> items,
			BigDecimal totalAmount,
			OrderStatus status,
			Instant createdAt,
			Instant updatedAt,
			Long version) {
	}

	public record OrderItemResponse(
			UUID id,
			Integer productId,
			String productName,
			int quantity,
			BigDecimal unitPrice,
			BigDecimal subtotal) {
	}

	public record ErrorResponse(String message) {
	}
}
