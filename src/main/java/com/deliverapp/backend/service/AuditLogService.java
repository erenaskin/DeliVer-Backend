package com.deliverapp.backend.service;

import com.deliverapp.backend.model.AuditLog;
import com.deliverapp.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public void log(String action, String username, String details) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .username(username)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
        auditLogRepository.save(log);
    }
}
