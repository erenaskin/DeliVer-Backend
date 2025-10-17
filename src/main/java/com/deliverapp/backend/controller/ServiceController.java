package com.deliverapp.backend.controller;

import com.deliverapp.backend.dto.request.ServiceRequest;
import com.deliverapp.backend.dto.response.ServiceResponse;

// Do not import Service entity to avoid ambiguity with org.springframework.stereotype.Service
import com.deliverapp.backend.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
    @Autowired
    private ServiceService serviceService;

    @GetMapping
    public List<ServiceResponse> getAllServices() {
        return serviceService.getAllServices().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Long id) {
        return serviceService.getServiceById(id)
            .map(service -> ResponseEntity.ok(toResponse(service)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ServiceResponse createService(@RequestBody ServiceRequest request) {
        com.deliverapp.backend.model.Service service = new com.deliverapp.backend.model.Service();
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setCreatedAt(LocalDateTime.now());
        service.setUpdatedAt(LocalDateTime.now());
        return toResponse(serviceService.createService(service));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateService(@PathVariable Long id, @RequestBody ServiceRequest request) {
        com.deliverapp.backend.model.Service updated = new com.deliverapp.backend.model.Service();
        updated.setName(request.getName());
        updated.setDescription(request.getDescription());
        updated.setUpdatedAt(LocalDateTime.now());
        com.deliverapp.backend.model.Service result = serviceService.updateService(id, updated);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toResponse(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    private ServiceResponse toResponse(com.deliverapp.backend.model.Service service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .key(service.getKey())
                .icon(service.getIcon())
                .sortOrder(service.getSortOrder())
                .isActive(service.getIsActive())
                .createdAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt())
                .build();
    }
}
