package com.winter.ecommerce.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.winter.ecommerce.dto.CustomerRequest;
import com.winter.ecommerce.entity.Customer;
import com.winter.ecommerce.repository.CustomerRepository;
import com.winter.ecommerce.service.CustomerService;

@Service
@Validated
public class CustomerServiceImpl implements CustomerService {

	private final CustomerRepository customerRepository;

	public CustomerServiceImpl(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Customer> findCustomers() {
		return customerRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Customer> findCustomer(Long id) {
		return customerRepository.findById(id);
	}

	@Override
	@Transactional
	public Customer createCustomer(CustomerRequest request) {
		return customerRepository.save(new Customer(request.name(), request.email()));
	}

	@Override
	@Transactional
	public Optional<Customer> updateCustomer(Long id, CustomerRequest request) {
		return customerRepository.findById(id).map(customer -> {
			customer.setName(request.name());
			customer.setEmail(request.email());
			return customerRepository.save(customer);
		});
	}

	@Override
	@Transactional
	public boolean deleteCustomer(Long id) {
		if (!customerRepository.existsById(id)) {
			return false;
		}
		customerRepository.deleteById(id);
		return true;
	}
}
