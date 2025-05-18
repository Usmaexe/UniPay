package com.unipay.service.audit_log;

import com.unipay.exception.BusinessException;
import com.unipay.exception.ExceptionPayloadFactory;
import com.unipay.models.AuditLog;
import com.unipay.models.User;
import com.unipay.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void createAuditLog(User user, String action, String details) {
        log.debug("Creating AuditLog for userId={} with action={}", user.getId(), action);

        AuditLog auditLog = AuditLog.create(action, details, LocalDateTime.now());
        auditLog.setUser(user);
        user.addAuditLog(auditLog);

        auditLogRepository.save(auditLog);

        log.info("AuditLog created successfully: id={}, userId={}", auditLog.getId(), user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLog findByIdOrAction(String auditId, String action) {
        log.debug("Fetching AuditLog with id={} or action={}", auditId, action);

        return auditLogRepository.findByIdOrAction(auditId, action)
                .orElseThrow(() -> new BusinessException(ExceptionPayloadFactory.AUDIT_LOG_NOT_FOUND.get()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByUserId(Pageable pageable, String userId) {
        log.debug("Fetching paginated AuditLogs for userId={}", userId);

        Page<AuditLog> auditLogs = auditLogRepository.getAuditLogsByUser_Id(pageable, userId);

        log.info("Fetched {} AuditLogs for userId={}", auditLogs.getTotalElements(), userId);
        return auditLogs;
    }
}
