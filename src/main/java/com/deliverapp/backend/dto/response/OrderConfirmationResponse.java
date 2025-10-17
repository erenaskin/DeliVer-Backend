package com.deliverapp.backend.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderConfirmationResponse {
    private Long orderId;
    private String orderNumber;
    private String orderStatus;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private String phoneNumber;
    private String notes;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime createdAt;
    private String message;
    private Boolean success;
}