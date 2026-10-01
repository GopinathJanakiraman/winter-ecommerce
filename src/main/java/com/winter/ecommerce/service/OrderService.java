package com.winter.ecommerce.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.winter.ecommerce.dto.OrderRequest;
import com.winter.ecommerce.dto.OrderResponse;
import com.winter.ecommerce.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface OrderService {

	List<OrderResponse> findOrders(OrderStatus status, Integer customerId);

	Optional<OrderResponse> findOrder(UUID id);

	OrderResponse createOrder(@NotNull @Valid OrderRequest request);

	Optional<OrderResponse> updateOrder(UUID id, @NotNull @Valid OrderRequest request);

	Optional<OrderResponse> cancelOrder(UUID id);

	boolean deleteOrder(UUID id);

	void processPendingOrders();
}
