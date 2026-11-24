package com.certsniper.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.certsniper.admin.entity.AdminAuditLog;
import com.certsniper.admin.repository.AdminAuditLogRepository;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AdminAuditLogRepository adminAuditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @ParameterizedTest
    @ValueSource(strings = {"UPDATE_PROFILE", "SUSPEND", "UNSUSPEND", "STORY_HIDE"})
    void 사유가_필요한_작업에_사유가_없으면_입력_오류(String action) {
        assertThatThrownBy(() -> auditLogService.record(1L, 2L, action, "users", "2", null, null, " "))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
        verify(adminAuditLogRepository, never()).save(any());
    }

    @Test
    void 사유가_필요_없는_작업은_사유_없이_저장한다() {
        auditLogService.record(1L, null, "CREATE", "notices", "3", null, "{\"title\":\"공지\"}", null);

        ArgumentCaptor<AdminAuditLog> captor = ArgumentCaptor.forClass(AdminAuditLog.class);
        verify(adminAuditLogRepository).save(captor.capture());
        AdminAuditLog log = captor.getValue();
        assertThat(log.getAction()).isEqualTo("CREATE");
        assertThat(log.getTargetTable()).isEqualTo("notices");
        assertThat(log.getTargetId()).isEqualTo("3");
        assertThat(log.getRequestId()).isNull();
        assertThat(log.getClientIp()).isNull();
    }
}
