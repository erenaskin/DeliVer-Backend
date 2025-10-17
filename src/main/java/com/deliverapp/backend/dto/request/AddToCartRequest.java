package com.deliverapp.backend.dto.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddToCartRequest {
    private Long productId;
    private Long productVariantId; // Optional
    private Integer quantity;
    private Map<String, Object> selectedOptions; // For options like sauces, extras
    private String notes; // Special instructions
}