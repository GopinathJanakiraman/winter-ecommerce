package com.winter.ecommerce.service;

import java.util.List;
import java.util.Optional;

import com.winter.ecommerce.dto.CustomerRequest;
import com.winter.ecommerce.entity.Customer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface CustomerService {

	List<Customer> findCustomers();

	Optional<Customer> findCustomer(Long id);

	Customer createCustomer(@NotNull @Valid CustomerRequest request);

	Optional<Customer> updateCustomer(Long id, @NotNull @Valid CustomerRequest request);

	boolean deleteCustomer(Long id);
}
