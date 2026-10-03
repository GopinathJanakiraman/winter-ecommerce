package com.winter.ecommerce.dto;

import com.winter.ecommerce.entity.Customer;

public record CustomerResponse(Long id, String name, String email) {

	public static CustomerResponse from(Customer customer) {
		return new CustomerResponse(customer.getId(), customer.getName(), customer.getEmail());
	}
}
