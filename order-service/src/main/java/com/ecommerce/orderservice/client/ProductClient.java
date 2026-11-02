package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.dto.DeductStockRequest;
import com.ecommerce.orderservice.dto.ProductResponse;
import com.ecommerce.orderservice.dto.RestoreStockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductResponse getProductById(@PathVariable("id") Long id);

    @PostMapping("/api/products/{id}/deduct-stock")
    ProductResponse deductStock(@PathVariable("id") Long id, @RequestBody DeductStockRequest request);

    @PostMapping("/api/products/{id}/restore-stock")
    ProductResponse restoreStock(@PathVariable("id") Long id, @RequestBody RestoreStockRequest request);
}
