package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.client.UserClient;
import com.ecommerce.orderservice.dto.CreateOrderRequest;
import com.ecommerce.orderservice.dto.DeductStockRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.ProductResponse;
import com.ecommerce.orderservice.dto.RestoreStockRequest;
import com.ecommerce.orderservice.dto.UserResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.exception.InsufficientStockException;
import com.ecommerce.orderservice.exception.OrderNotFoundException;
import com.ecommerce.orderservice.exception.ProductNotFoundException;
import com.ecommerce.orderservice.exception.UserNotFoundException;
import com.ecommerce.orderservice.repository.OrderRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final UserClient userClient;

    // Explicit constructor dependency injection
    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductClient productClient,
                            UserClient userClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.userClient = userClient;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. Precondition: Validate user existence via User Service
        UserResponse user;
        try {
            user = userClient.getUserById(request.getUserId());
            if (user == null) {
                throw new UserNotFoundException(request.getUserId());
            }
        } catch (FeignException.NotFound ex) {
            throw new UserNotFoundException(request.getUserId());
        }

        // 2. Precondition: Validate quantity is at least 1 (defensive check; also covered by @Min(1) on DTO)
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        // 3. Precondition: Validate product existence via Product Service
        ProductResponse product;
        try {
            product = productClient.getProductById(request.getProductId());
            if (product == null) {
                throw new ProductNotFoundException(request.getProductId());
            }
        } catch (FeignException.NotFound ex) {
            throw new ProductNotFoundException(request.getProductId());
        }

        // 4. Precondition: Preliminary stock check from product snapshot
        int availableStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        if (availableStock < request.getQuantity()) {
            throw new InsufficientStockException(request.getProductId(), availableStock, request.getQuantity());
        }

        // 5. Server-side price calculation using actual product price * requested quantity (BigDecimal)
        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        // 6. Concurrency-safe atomic stock deduction in Product Service via OpenFeign
        try {
            productClient.deductStock(request.getProductId(), new DeductStockRequest(request.getQuantity()));
        } catch (FeignException.NotFound ex) {
            throw new ProductNotFoundException(request.getProductId());
        } catch (FeignException.BadRequest ex) {
            throw new InsufficientStockException(request.getProductId(), availableStock, request.getQuantity());
        }

        // 7. Save Order and handle compensation if order persistence fails
        Order savedOrder;
        try {
            Order order = Order.builder()
                    .userId(request.getUserId())
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .totalAmount(totalAmount)
                    .status("CREATED")
                    .build();

            // Force immediate database write to detect any constraint or SQL errors inside this try block
            savedOrder = orderRepository.saveAndFlush(order);
        } catch (Exception ex) {
            // Compensating action: restore the deducted stock in Product Service
            log.warn("Order persistence failed for user {} and product {}. Triggering compensating stock restoration. Cause: {}",
                    request.getUserId(), request.getProductId(), ex.getMessage());
            try {
                productClient.restoreStock(request.getProductId(), new RestoreStockRequest(request.getQuantity()));
                log.info("Successfully compensated stock for product ID {}", request.getProductId());
            } catch (Exception compensationEx) {
                log.error("CRITICAL: Failed to compensate stock for product ID {} after order persistence failure. " +
                                "Manual reconciliation required. Error: {}",
                        request.getProductId(), compensationEx.getMessage(), compensationEx);
            }
            throw ex;
        }

        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return mapToOrderResponse(order);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .build();
    }
}
