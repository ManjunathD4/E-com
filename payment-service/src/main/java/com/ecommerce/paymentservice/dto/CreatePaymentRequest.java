package com.ecommerce.paymentservice.dto;

import lombok.NoArgsConstructor;

/**
 * Convenience alias for PaymentRequest following the Create*Request naming convention.
 */
@NoArgsConstructor
public class CreatePaymentRequest extends PaymentRequest {

    public CreatePaymentRequest(Long orderId, java.math.BigDecimal amount, String paymentMethod) {
        super(orderId, amount, paymentMethod);
    }
}
