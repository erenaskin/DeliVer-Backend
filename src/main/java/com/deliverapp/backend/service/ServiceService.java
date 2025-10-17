package com.deliverapp.backend.service;

import com.deliverapp.backend.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceService {
    @Autowired
    private ServiceRepository serviceRepository;

    public List<com.deliverapp.backend.model.Service> getAllServices() {
        return serviceRepository.findAll();
    }

    public Optional<com.deliverapp.backend.model.Service> getServiceById(Long id) {
        return serviceRepository.findById(id);
    }

    public com.deliverapp.backend.model.Service createService(com.deliverapp.backend.model.Service service) {
        return serviceRepository.save(service);
    }

    public com.deliverapp.backend.model.Service updateService(Long id, com.deliverapp.backend.model.Service updatedService) {
        return serviceRepository.findById(id)
                .map(service -> {
                    service.setName(updatedService.getName());
                    service.setDescription(updatedService.getDescription());
                    service.setUpdatedAt(updatedService.getUpdatedAt());
                    return serviceRepository.save(service);
                })
                .orElse(null);
    }

    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }
}
