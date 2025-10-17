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
public class UpdateCartItemRequest {
    private Integer quantity;
    private Map<String, Object> selectedOptions; // Update selected options
    private String notes; // Update special instructions
}