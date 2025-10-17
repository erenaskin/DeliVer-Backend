package com.deliverapp.backend.repository;

import com.deliverapp.backend.model.Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ServiceRepositoryTest {
    @Autowired
    private ServiceRepository serviceRepository;

    @Test
    void testSaveAndFindService() {
        Service service = new Service();
        service.setName("Test Service");
        service.setDescription("Test Description");
        service.setCreatedAt(java.time.LocalDateTime.now());
        service.setUpdatedAt(java.time.LocalDateTime.now());
        Service saved = serviceRepository.save(service);
        assertThat(saved.getId()).isNotNull();
        assertThat(serviceRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void testFindAllServices() {
        Service service1 = new Service();
        service1.setName("Service 1");
        service1.setDescription("Desc 1");
        service1.setCreatedAt(java.time.LocalDateTime.now());
        service1.setUpdatedAt(java.time.LocalDateTime.now());
        serviceRepository.save(service1);

        Service service2 = new Service();
        service2.setName("Service 2");
        service2.setDescription("Desc 2");
        service2.setCreatedAt(java.time.LocalDateTime.now());
        service2.setUpdatedAt(java.time.LocalDateTime.now());
        serviceRepository.save(service2);

        List<Service> services = serviceRepository.findAll();
        assertThat(services.size()).isGreaterThanOrEqualTo(2);
    }
}
