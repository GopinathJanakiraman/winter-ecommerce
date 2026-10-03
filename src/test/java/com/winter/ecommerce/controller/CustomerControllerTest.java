package com.winter.ecommerce.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import com.winter.ecommerce.entity.Customer;
import com.winter.ecommerce.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

	@Mock
	private CustomerService customerService;

	@InjectMocks
	private CustomerController customerController;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(customerController)
				.setControllerAdvice(new ApiExceptionHandler())
				.build();
	}

	@Test
	void rejectsInvalidCustomerEmailBeforeCallingService() throws Exception {
		mockMvc.perform(post("/api/customers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Alex Morgan","email":"not-an-email"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("email must be a valid email address."));

		verifyNoInteractions(customerService);
	}

	@Test
	void returnsCustomerResponseFromEntity() throws Exception {
		when(customerService.findCustomer(7L)).thenReturn(Optional.of(new Customer("Alex Morgan", "alex@example.com")));

		mockMvc.perform(get("/api/customers/7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Alex Morgan"))
				.andExpect(jsonPath("$.email").value("alex@example.com"));
	}
}
