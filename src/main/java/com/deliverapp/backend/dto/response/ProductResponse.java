package com.deliverapp.backend.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String shortDescription;
    private Long serviceId;
    private String serviceName;
    private Long categoryId;
    private String categoryName;
    private String key;
    private String sku;
    private String productType;
    private Map<String, Object> attributes;
    private Boolean isActive;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Pricing information
    private List<ProductPricingResponse> pricing;
    
    // Variants
    private List<ProductVariantResponse> variants;
    
    // Option Groups
    private List<OptionGroupResponse> optionGroups;
    
    // Flags
    private List<ProductFlagResponse> flags;
}