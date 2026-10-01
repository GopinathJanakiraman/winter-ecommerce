package com.winter.ecommerce;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.winter.ecommerce.service.OrderService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EcommerceApplicationTests {

	@Autowired
	private OrderService orderService;

	@Test
	void contextLoads() {
	}

	@Test
	void validatesServiceInterfaceParametersWithoutConstraintDeclarationErrors() {
		assertThrows(ConstraintViolationException.class, () -> orderService.createOrder(null));
	}
}
