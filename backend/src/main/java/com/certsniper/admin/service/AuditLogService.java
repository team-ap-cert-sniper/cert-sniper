package com.certsniper.admin.service;

import com.certsniper.admin.repository.AdminAuditLogRepository;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AdminAuditLogRepository adminAuditLogRepository;

    /** admin 변경 작업마다: 변경 전·후·관리자·사유 기록 */
    @Transactional
    public void record(Long adminId, Long targetUserId, String action, String targetTable, String targetId, String beforeJson, String afterJson, String reason) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
