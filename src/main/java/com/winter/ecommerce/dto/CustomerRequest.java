package com.winter.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
		@NotBlank(message = "name is required.") String name,
		@NotBlank(message = "email is required.")
		@Email(message = "email must be a valid email address.") String email) {
}
