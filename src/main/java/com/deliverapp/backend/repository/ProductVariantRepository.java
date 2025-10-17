package com.deliverapp.backend.repository;

import com.deliverapp.backend.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    
    List<ProductVariant> findByProductIdAndIsActiveTrue(Long productId);
    List<ProductVariant> findByProductIdAndIsActiveTrueOrderBySortOrderAsc(Long productId);
    
    Optional<ProductVariant> findByIdAndIsActiveTrue(Long id);
    Optional<ProductVariant> findBySkuAndIsActiveTrue(String sku);
}