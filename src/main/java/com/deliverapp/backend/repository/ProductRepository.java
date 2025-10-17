package com.deliverapp.backend.repository;

import com.deliverapp.backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @EntityGraph(attributePaths = {"service", "category"})
    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"service", "category"})
    Page<Product> findByServiceIdAndIsActiveTrue(Long serviceId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"service", "category"})
    Optional<Product> findByIdAndIsActiveTrue(Long id);
    
    @EntityGraph(attributePaths = {"service", "category"})
    Optional<Product> findByKeyAndIsActiveTrue(String key);
    
    @EntityGraph(attributePaths = {"service", "category"})
    Optional<Product> findBySkuAndIsActiveTrue(String sku);
    
    @EntityGraph(attributePaths = {"service", "category"})
    Page<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);
    
    @EntityGraph(attributePaths = {"service", "category"})
    Page<Product> findByNameContainingIgnoreCaseAndServiceIdAndIsActiveTrue(String name, Long serviceId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"service", "category"})
    Page<Product> findByNameContainingIgnoreCaseAndCategoryIdAndIsActiveTrue(String name, Long categoryId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"service", "category"})
    Page<Product> findByNameContainingIgnoreCaseAndServiceIdAndCategoryIdAndIsActiveTrue(String name, Long serviceId, Long categoryId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"service", "category"})
    Page<Product> findByServiceIdAndCategoryIdAndIsActiveTrue(Long serviceId, Long categoryId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"service", "category"})
    Page<Product> findByIsActiveTrue(Pageable pageable);
    
    @EntityGraph(attributePaths = {"service", "category"})
    List<Product> findByServiceIdAndIsActiveTrueOrderBySortOrderAsc(Long serviceId);
    
    @EntityGraph(attributePaths = {"service", "category"})
    List<Product> findByCategoryIdAndIsActiveTrueOrderBySortOrderAsc(Long categoryId);
}