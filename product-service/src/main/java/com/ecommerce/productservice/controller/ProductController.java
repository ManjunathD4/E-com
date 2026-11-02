package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.DeductStockRequest;
import com.ecommerce.productservice.dto.ProductRequest;
import com.ecommerce.productservice.dto.ProductResponse;
import com.ecommerce.productservice.dto.RestoreStockRequest;
import com.ecommerce.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // Explicit constructor dependency injection
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/{id}/deduct-stock")
    public ResponseEntity<ProductResponse> deductStock(
            @PathVariable Long id,
            @Valid @RequestBody DeductStockRequest request) {
        ProductResponse response = productService.deductStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/restore-stock")
    public ResponseEntity<ProductResponse> restoreStock(
            @PathVariable Long id,
            @Valid @RequestBody RestoreStockRequest request) {
        ProductResponse response = productService.restoreStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }
}
