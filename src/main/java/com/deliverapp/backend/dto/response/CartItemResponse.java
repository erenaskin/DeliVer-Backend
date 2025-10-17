package com.deliverapp.backend.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long cartId;
    private Long productId;
    private String productName;
    private String productImage; // Main product image
    private Long productVariantId;
    private String variantName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private Map<String, Object> selectedOptions;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}