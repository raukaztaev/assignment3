package com.example.mvp.audit;

import com.example.mvp.entity.AuditLog;
import com.example.mvp.entity.UserEntity;
import com.example.mvp.repository.AuditLogRepository;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void write(UserEntity actor, String action, String entityType, Long entityId, String details) {
        AuditLog log = new AuditLog();
        log.setActorUserId(actor.getId());
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setCreatedAt(OffsetDateTime.now());
        auditLogRepository.save(log);
    }
}
