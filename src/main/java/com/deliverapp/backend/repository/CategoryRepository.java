package com.deliverapp.backend.repository;

import com.deliverapp.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByIsActiveTrueOrderBySortOrderAsc();
    
    List<Category> findByServiceIdAndIsActiveTrue(Long serviceId);
    List<Category> findByServiceIdAndIsActiveTrueOrderBySortOrderAsc(Long serviceId);
    
    List<Category> findByServiceIdAndParentIdIsNullAndIsActiveTrue(Long serviceId);
    List<Category> findByServiceIdAndParentIdIsNullAndIsActiveTrueOrderBySortOrderAsc(Long serviceId);
    
    List<Category> findByParentIdAndIsActiveTrue(Long parentId);
    List<Category> findByParentIdAndIsActiveTrueOrderBySortOrderAsc(Long parentId);
    
    Optional<Category> findByIdAndIsActiveTrue(Long id);
    Optional<Category> findByKeyAndIsActiveTrue(String key);
    
    @Query("SELECT c FROM Category c WHERE c.service.id = :serviceId AND c.parent IS NULL AND c.isActive = true ORDER BY c.sortOrder")
    List<Category> findRootCategoriesByServiceId(@Param("serviceId") Long serviceId);
}