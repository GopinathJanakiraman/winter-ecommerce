package com.winter.ecommerce.dto;

import java.util.List;

import com.winter.ecommerce.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
		@NotNull(message = "customerId is required.") Integer customerId,
		OrderStatus status,
		@NotEmpty(message = "At least one order item is required.")
		List<@NotNull(message = "Order items cannot be null.") @Valid OrderItemRequest> items) {
}
