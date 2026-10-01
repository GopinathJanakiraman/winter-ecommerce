package com.winter.ecommerce.service;

public class OrderNotCancellableException extends RuntimeException {

	public OrderNotCancellableException() {
		super("Only pending orders can be cancelled.");
	}
}
