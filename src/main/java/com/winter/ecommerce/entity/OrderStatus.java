package com.winter.ecommerce.entity;

/**
 * Lifecycle states supported by the order API and background processor.
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
