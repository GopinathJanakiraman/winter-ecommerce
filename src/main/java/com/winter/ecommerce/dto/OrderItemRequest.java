package com.winter.ecommerce.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
		@NotNull(message = "productId is required.") Integer productId,
		@NotBlank(message = "productName is required.") String productName,
		@Positive(message = "quantity must be positive.") int quantity,
		@NotNull(message = "unitPrice is required.")
		@DecimalMin(value = "0.00", message = "unitPrice must be non-negative.") BigDecimal unitPrice) {
}
