package com.certsniper.admin.service;

import com.certsniper.admin.entity.AdminAuditLog;
import com.certsniper.admin.repository.AdminAuditLogRepository;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    /** 사유가 꼭 있어야 하는 작업 (설계서 admin_audit_logs.reason, 서비스에서 검사) */
    private static final Set<String> REASON_REQUIRED_ACTIONS = Set.of("UPDATE_PROFILE", "SUSPEND", "UNSUSPEND", "STORY_HIDE");

    private final AdminAuditLogRepository adminAuditLogRepository;

    /** admin 변경 작업마다: 변경 전·후·관리자·사유 기록 */
    @Transactional
    public void record(Long adminId, Long targetUserId, String action, String targetTable, String targetId, String beforeJson, String afterJson, String reason) {
        if (REASON_REQUIRED_ACTIONS.contains(action) && (reason == null || reason.isBlank())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        adminAuditLogRepository.save(
                new AdminAuditLog(adminId, targetUserId, action, targetTable, targetId, beforeJson, afterJson, reason));
    }
}
