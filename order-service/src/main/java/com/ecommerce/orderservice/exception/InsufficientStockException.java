package com.ecommerce.orderservice.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(Long productId, Integer available, Integer requested) {
        super(String.format("Insufficient stock for product ID %d. Available: %d, Requested: %d",
                productId, available, requested));
    }
}
