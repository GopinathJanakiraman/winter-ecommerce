package com.winter.ecommerce.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.winter.ecommerce.dto.CustomerRequest;
import com.winter.ecommerce.entity.Customer;
import com.winter.ecommerce.service.CustomerService;
import jakarta.validation.Valid;

/**
 * HTTP adapter for customer operations; customer persistence is handled by CustomerService.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@GetMapping
	public List<Customer> getCustomers() {
		return customerService.findCustomers();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
		return customerService.findCustomer(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Customer> createCustomer(@Valid @RequestBody CustomerRequest request) {
		Customer customer = customerService.createCustomer(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(customer.getId())
				.toUri();
		return ResponseEntity.created(location).body(customer);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Customer> updateCustomer(
			@PathVariable Long id,
			@Valid @RequestBody CustomerRequest request) {
		return customerService.updateCustomer(id, request)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
		return customerService.deleteCustomer(id)
				? ResponseEntity.noContent().build()
				: ResponseEntity.notFound().build();
	}
}
