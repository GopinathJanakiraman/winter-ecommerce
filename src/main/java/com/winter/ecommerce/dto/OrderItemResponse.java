package com.winter.ecommerce.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
		UUID id,
		Integer productId,
		String productName,
		int quantity,
		BigDecimal unitPrice,
		BigDecimal subtotal) {
}
