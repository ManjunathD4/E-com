package com.ecommerce.productservice.dto;

import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
public class CreateProductRequest extends ProductRequest {

    public CreateProductRequest(String name, String description, BigDecimal price, Integer stockQuantity) {
        super(name, description, price, stockQuantity);
    }
}
