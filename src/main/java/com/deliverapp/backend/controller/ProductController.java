package com.deliverapp.backend.controller;

import com.deliverapp.backend.dto.response.*;
import com.deliverapp.backend.model.*;
import com.deliverapp.backend.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Transactional(readOnly = true)
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductPricingRepository productPricingRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OptionGroupRepository optionGroupRepository;
    private final OptionValueRepository optionValueRepository;
    private final ProductFlagRepository productFlagRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sortOrder").ascending());
        Page<Product> products = productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        
        List<ProductResponse> response = products.getContent().stream()
                .map(this::convertToSimpleResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<ProductResponse>> getProductsByService(
            @PathVariable Long serviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sortOrder").ascending());
        Page<Product> products = productRepository.findByServiceIdAndIsActiveTrue(serviceId, pageable);
        
        List<ProductResponse> response = products.getContent().stream()
                .map(this::convertToSimpleResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return productRepository.findByIdAndIsActiveTrue(id)
                .map(product -> ResponseEntity.ok(convertToDetailedResponse(product)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sortOrder").ascending());
        Page<Product> products;
        
        // Handle empty or null query
        boolean hasQuery = query != null && !query.trim().isEmpty();
        
        if (serviceId != null && categoryId != null) {
            if (hasQuery) {
                products = productRepository.findByNameContainingIgnoreCaseAndServiceIdAndCategoryIdAndIsActiveTrue(
                    query, serviceId, categoryId, pageable);
            } else {
                products = productRepository.findByServiceIdAndCategoryIdAndIsActiveTrue(serviceId, categoryId, pageable);
            }
        } else if (serviceId != null) {
            if (hasQuery) {
                products = productRepository.findByNameContainingIgnoreCaseAndServiceIdAndIsActiveTrue(
                    query, serviceId, pageable);
            } else {
                products = productRepository.findByServiceIdAndIsActiveTrue(serviceId, pageable);
            }
        } else if (categoryId != null) {
            if (hasQuery) {
                products = productRepository.findByNameContainingIgnoreCaseAndCategoryIdAndIsActiveTrue(
                    query, categoryId, pageable);
            } else {
                products = productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
            }
        } else {
            if (hasQuery) {
                products = productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(query, pageable);
            } else {
                products = productRepository.findByIsActiveTrue(pageable);
            }
        }
        
        List<ProductResponse> response = products.getContent().stream()
                .map(this::convertToSimpleResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    // Simple response for list views (with basic pricing information)
    private ProductResponse convertToSimpleResponse(Product product) {
        // Get basic pricing information for list views
        List<ProductPricing> pricingList = productPricingRepository.findByProductIdAndIsActiveTrue(product.getId());
        List<ProductPricingResponse> pricing = pricingList.stream()
                .map(this::convertToPricingResponse)
                .collect(Collectors.toList());
        
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .serviceId(product.getService().getId())
                .serviceName(product.getService().getName())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .key(product.getKey())
                .sku(product.getSku())
                .productType(product.getProductType().toString())
                .attributes(fromJson(product.getAttributesJson()))
                .isActive(product.getIsActive())
                .sortOrder(product.getSortOrder())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .pricing(pricing) // Add basic pricing to simple response
                .variants(Collections.emptyList()) // Empty array for simple response (performance)
                .optionGroups(Collections.emptyList()) // Empty array for simple response (performance)  
                .flags(Collections.emptyList()) // Empty array for simple response (performance)
                .build();
    }
    
    // Detailed response for single product view (with all relationships)
    private ProductResponse convertToDetailedResponse(Product product) {
        ProductResponse response = convertToSimpleResponse(product);
        
        // Note: Pricing is already included in simple response
        
        // Add variants
        List<ProductVariant> variantList = productVariantRepository.findByProductIdAndIsActiveTrueOrderBySortOrderAsc(product.getId());
        response.setVariants(variantList.stream()
                .map(this::convertToVariantResponse)
                .collect(Collectors.toList()));
        
        // Add option groups with values
        List<OptionGroup> optionGroupList = optionGroupRepository.findByProductIdAndIsActiveTrueOrderBySortOrderAsc(product.getId());
        response.setOptionGroups(optionGroupList.stream()
                .map(this::convertToOptionGroupResponse)
                .collect(Collectors.toList()));
        
        // Add flags
        List<ProductFlag> flagList = productFlagRepository.findByProductIdAndIsActiveTrue(product.getId());
        response.setFlags(flagList.stream()
                .map(this::convertToFlagResponse)
                .collect(Collectors.toList()));
        
        return response;
    }
    
    private ProductPricingResponse convertToPricingResponse(ProductPricing pricing) {
        return ProductPricingResponse.builder()
                .id(pricing.getId())
                .productId(pricing.getProduct().getId())
                .pricingType(pricing.getPricingType().toString())
                .basePrice(pricing.getBasePrice())
                .minPrice(pricing.getMinPrice())
                .maxPrice(pricing.getMaxPrice())
                .currency(pricing.getCurrency())
                .pricingRules(fromJson(pricing.getPricingRulesJson()))
                .isActive(pricing.getIsActive())
                .effectiveFrom(pricing.getEffectiveFrom())
                .effectiveUntil(pricing.getEffectiveUntil())
                .createdAt(pricing.getCreatedAt())
                .updatedAt(pricing.getUpdatedAt())
                .build();
    }
    
    private ProductVariantResponse convertToVariantResponse(ProductVariant variant) {
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .productId(variant.getProduct().getId())
                .variantName(variant.getVariantName())
                .sku(variant.getSku())
                .attributes(fromJson(variant.getAttributesJson()))
                .priceModifier(variant.getPriceModifier())
                .isActive(variant.getIsActive())
                .sortOrder(variant.getSortOrder())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }
    
    private OptionGroupResponse convertToOptionGroupResponse(OptionGroup optionGroup) {
        List<OptionValue> optionValues = optionValueRepository.findByOptionGroupIdAndIsActiveTrueOrderBySortOrderAsc(optionGroup.getId());
        
        return OptionGroupResponse.builder()
                .id(optionGroup.getId())
                .productId(optionGroup.getProduct().getId())
                .name(optionGroup.getName())
                .description(optionGroup.getDescription())
                .optionType(optionGroup.getOptionType().toString())
                .isRequired(optionGroup.getIsRequired())
                .isActive(optionGroup.getIsActive())
                .sortOrder(optionGroup.getSortOrder())
                .minSelections(optionGroup.getMinSelections())
                .maxSelections(optionGroup.getMaxSelections())
                .options(optionValues.stream()
                        .map(this::convertToOptionValueResponse)
                        .collect(Collectors.toList()))
                .createdAt(optionGroup.getCreatedAt())
                .updatedAt(optionGroup.getUpdatedAt())
                .build();
    }
    
    private OptionValueResponse convertToOptionValueResponse(OptionValue optionValue) {
        return OptionValueResponse.builder()
                .id(optionValue.getId())
                .optionGroupId(optionValue.getOptionGroup().getId())
                .name(optionValue.getName())
                .valueText(optionValue.getValueText())
                .priceModifier(optionValue.getPriceModifier())
                .isActive(optionValue.getIsActive())
                .sortOrder(optionValue.getSortOrder())
                .createdAt(optionValue.getCreatedAt())
                .updatedAt(optionValue.getUpdatedAt())
                .build();
    }
    
    private ProductFlagResponse convertToFlagResponse(ProductFlag flag) {
        return ProductFlagResponse.builder()
                .id(flag.getId())
                .productId(flag.getProduct().getId())
                .flagKey(flag.getFlagKey())
                                .flagValue(flag.getFlagValue())
                .flagType(flag.getFlagType().toString())
                .description(flag.getDescription())
                .isActive(flag.getIsActive())
                .createdAt(flag.getCreatedAt())
                .updatedAt(flag.getUpdatedAt())
                .build();
    }

    private java.util.Map<String, Object> fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<java.util.Map<String, Object>>() {});
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // Log the error and return an empty map or handle it as needed
            return new java.util.HashMap<>();
        }
    }
}