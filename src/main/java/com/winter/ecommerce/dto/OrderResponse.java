package com.winter.ecommerce.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.winter.ecommerce.entity.OrderStatus;

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
