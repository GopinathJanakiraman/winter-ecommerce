package com.winter.ecommerce.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.winter.ecommerce.dto.OrderItemRequest;
import com.winter.ecommerce.dto.OrderItemResponse;
import com.winter.ecommerce.dto.OrderRequest;
import com.winter.ecommerce.dto.OrderResponse;
import com.winter.ecommerce.entity.Order;
import com.winter.ecommerce.entity.OrderItem;
import com.winter.ecommerce.entity.OrderStatus;
import com.winter.ecommerce.repository.OrderRepository;
import com.winter.ecommerce.service.OrderNotCancellableException;
import com.winter.ecommerce.service.OrderService;

@Service
@Validated
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;

	public OrderServiceImpl(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<OrderResponse> findOrders(OrderStatus status, Integer customerId) {
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
		return orders.stream().map(OrderServiceImpl::toResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<OrderResponse> findOrder(UUID id) {
		return orderRepository.findById(id).map(OrderServiceImpl::toResponse);
	}

	@Override
	@Transactional
	public OrderResponse createOrder(OrderRequest request) {
		Instant now = Instant.now();
		Order order = new Order();
		order.setId(UUID.randomUUID());
		order.setCustomerId(request.customerId());
		order.setStatus(request.status() == null ? OrderStatus.PENDING : request.status());
		order.setCreatedAt(now);
		order.setUpdatedAt(now);
		replaceItems(order, request.items());
		return toResponse(orderRepository.save(order));
	}

	@Override
	@Transactional
	public Optional<OrderResponse> updateOrder(UUID id, OrderRequest request) {
		return orderRepository.findById(id).map(order -> {
			order.setCustomerId(request.customerId());
			if (request.status() != null) {
				order.setStatus(request.status());
			}
			replaceItems(order, request.items());
			order.setUpdatedAt(Instant.now());
			return toResponse(orderRepository.save(order));
		});
	}

	@Override
	@Transactional
	public Optional<OrderResponse> cancelOrder(UUID id) {
		return orderRepository.findById(id).map(order -> {
			if (order.getStatus() != OrderStatus.PENDING) {
				throw new OrderNotCancellableException();
			}
			order.setStatus(OrderStatus.CANCELLED);
			order.setUpdatedAt(Instant.now());
			return toResponse(orderRepository.save(order));
		});
	}

	@Override
	@Transactional
	public boolean deleteOrder(UUID id) {
		if (!orderRepository.existsById(id)) {
			return false;
		}
		orderRepository.deleteById(id);
		return true;
	}

	@Override
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
}
