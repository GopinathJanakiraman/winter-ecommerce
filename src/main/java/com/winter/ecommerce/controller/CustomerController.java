package com.winter.ecommerce.controller;

import com.winter.ecommerce.entity.Customer;
import com.winter.ecommerce.repository.CustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

	private final CustomerRepository customerRepository;

	public CustomerController(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@GetMapping
	public List<Customer> getCustomers() {
		return customerRepository.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
		return customerRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Customer> createCustomer(@RequestBody CustomerRequest request) {
		Customer customer = customerRepository.save(new Customer(request.name(), request.email()));
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(customer.getId())
				.toUri();
		return ResponseEntity.created(location).body(customer);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Customer> updateCustomer(
			@PathVariable Long id,
			@RequestBody CustomerRequest request) {
		return customerRepository.findById(id)
				.map(customer -> {
					customer.setName(request.name());
					customer.setEmail(request.email());
					return ResponseEntity.ok(customerRepository.save(customer));
				})
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
		if (!customerRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		customerRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	public record CustomerRequest(String name, String email) {
	}
}
