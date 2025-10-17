package com.deliverapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<com.deliverapp.backend.model.Service, Long> {
    // Custom queries if needed
}
