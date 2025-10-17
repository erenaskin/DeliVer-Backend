package com.deliverapp.backend.controller;

import com.deliverapp.backend.dto.response.CategoryResponse;
import com.deliverapp.backend.model.Category;
import com.deliverapp.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Transactional(readOnly = true)
public class CategoryController {
    
    private final CategoryRepository categoryRepository;
    
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        List<CategoryResponse> response = categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByService(@PathVariable Long serviceId) {
        List<Category> categories = categoryRepository.findByServiceIdAndIsActiveTrueOrderBySortOrderAsc(serviceId);
        List<CategoryResponse> response = categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/service/{serviceId}/root")
    public ResponseEntity<List<CategoryResponse>> getRootCategoriesByService(@PathVariable Long serviceId) {
        List<Category> categories = categoryRepository.findByServiceIdAndParentIdIsNullAndIsActiveTrueOrderBySortOrderAsc(serviceId);
        List<CategoryResponse> response = categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/children")
    public ResponseEntity<List<CategoryResponse>> getChildCategories(@PathVariable Long id) {
        List<Category> categories = categoryRepository.findByParentIdAndIsActiveTrueOrderBySortOrderAsc(id);
        List<CategoryResponse> response = categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return categoryRepository.findByIdAndIsActiveTrue(id)
                .map(category -> ResponseEntity.ok(convertToResponse(category)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/key/{key}")
    public ResponseEntity<CategoryResponse> getCategoryByKey(@PathVariable String key) {
        return categoryRepository.findByKeyAndIsActiveTrue(key)
                .map(category -> ResponseEntity.ok(convertToResponse(category)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    private CategoryResponse convertToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .serviceId(category.getService().getId())
                .serviceName(category.getService().getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .key(category.getKey())
                .icon(category.getIcon())
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .hasChildren(category.getChildren() != null && !category.getChildren().isEmpty())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}