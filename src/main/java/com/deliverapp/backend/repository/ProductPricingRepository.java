package com.deliverapp.backend.repository;

import com.deliverapp.backend.model.ProductPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductPricingRepository extends JpaRepository<ProductPricing, Long> {
    
    List<ProductPricing> findByProductIdAndIsActiveTrue(Long productId);
    List<ProductPricing> findByProductIdAndIsActiveTrueOrderByEffectiveFromDesc(Long productId);
    
    Optional<ProductPricing> findByIdAndIsActiveTrue(Long id);
    Optional<ProductPricing> findByProductIdAndIsActiveTrueAndEffectiveFromLessThanEqualAndEffectiveUntilGreaterThanEqual(
            Long productId, java.time.LocalDateTime effectiveFrom, java.time.LocalDateTime effectiveUntil);
}