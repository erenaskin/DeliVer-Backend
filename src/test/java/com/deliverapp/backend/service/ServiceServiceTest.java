package com.deliverapp.backend.service;

import com.deliverapp.backend.model.Service;
import com.deliverapp.backend.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ServiceServiceTest {
    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceService serviceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllServices() {
        Service s1 = new Service();
        s1.setId(1L);
        s1.setName("A");
        s1.setDescription("Desc");
        s1.setCreatedAt(LocalDateTime.now());
        s1.setUpdatedAt(LocalDateTime.now());
        Service s2 = new Service();
        s2.setId(2L);
        s2.setName("B");
        s2.setDescription("Desc");
        s2.setCreatedAt(LocalDateTime.now());
        s2.setUpdatedAt(LocalDateTime.now());
        when(serviceRepository.findAll()).thenReturn(Arrays.asList(s1, s2));
        List<Service> result = serviceService.getAllServices();
        assertThat(result).hasSize(2);
    }

    @Test
    void testGetServiceById() {
        Service s = new Service();
        s.setId(1L);
        s.setName("A");
        s.setDescription("Desc");
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(s));
        Optional<Service> result = serviceService.getServiceById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("A");
    }

    @Test
    void testCreateService() {
        Service s = new Service();
        s.setName("A");
        s.setDescription("Desc");
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        when(serviceRepository.save(any(Service.class))).thenReturn(s);
        Service result = serviceService.createService(s);
        assertThat(result.getName()).isEqualTo("A");
    }

    @Test
    void testUpdateService() {
        Service existing = new Service();
        existing.setId(1L);
        existing.setName("Old");
        existing.setDescription("Old Desc");
        existing.setCreatedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());
        Service updated = new Service();
        updated.setName("New");
        updated.setDescription("New Desc");
        updated.setUpdatedAt(LocalDateTime.now());
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(serviceRepository.save(any(Service.class))).thenReturn(updated);
        Service result = serviceService.updateService(1L, updated);
        assertThat(result.getName()).isEqualTo("New");
    }

    @Test
    void testDeleteService() {
        doNothing().when(serviceRepository).deleteById(1L);
        serviceService.deleteService(1L);
        verify(serviceRepository, times(1)).deleteById(1L);
    }
}
