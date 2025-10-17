package com.deliverapp.backend.repository;

import com.deliverapp.backend.model.OptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionGroupRepository extends JpaRepository<OptionGroup, Long> {
    
    List<OptionGroup> findByProductIdAndIsActiveTrue(Long productId);
    List<OptionGroup> findByProductIdAndIsActiveTrueOrderBySortOrderAsc(Long productId);
    
    Optional<OptionGroup> findByIdAndIsActiveTrue(Long id);
}