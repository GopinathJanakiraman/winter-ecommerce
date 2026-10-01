package com.winter.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.winter.ecommerce.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
