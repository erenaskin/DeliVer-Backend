package com.deliverapp.backend.repository;

import com.deliverapp.backend.model.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionValueRepository extends JpaRepository<OptionValue, Long> {
    
    List<OptionValue> findByOptionGroupIdAndIsActiveTrue(Long optionGroupId);
    List<OptionValue> findByOptionGroupIdAndIsActiveTrueOrderBySortOrderAsc(Long optionGroupId);
    
    Optional<OptionValue> findByIdAndIsActiveTrue(Long id);
}