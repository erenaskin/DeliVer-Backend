package com.deliverapp.backend.repository;

import com.deliverapp.backend.model.ProductFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductFlagRepository extends JpaRepository<ProductFlag, Long> {
    
    List<ProductFlag> findByProductIdAndIsActiveTrue(Long productId);
    List<ProductFlag> findByProductIdAndFlagKeyAndIsActiveTrue(Long productId, String flagKey);
    
    Optional<ProductFlag> findByIdAndIsActiveTrue(Long id);
    Optional<ProductFlag> findFirstByProductIdAndFlagKeyAndIsActiveTrue(Long productId, String flagKey);
}