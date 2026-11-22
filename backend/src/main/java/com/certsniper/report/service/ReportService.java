package com.certsniper.report.service;

import com.certsniper.global.common.PageResponse;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.report.dto.MyReportResponse;
import com.certsniper.report.dto.ReportRequest;
import com.certsniper.report.entity.PassStoryReport;
import com.certsniper.report.repository.PassStoryModerationLogRepository;
import com.certsniper.report.repository.PassStoryReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final PassStoryReportRepository passStoryReportRepository;
    private final PassStoryModerationLogRepository passStoryModerationLogRepository;

    @Transactional
    public void reportPassStory(Long userId, Long storyId, ReportRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    public PageResponse<MyReportResponse> getMyReports(Long userId, int page, int size) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** admin 대시보드: 미처리 신고 수 */
    public long countPending() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** passstory 수정·삭제, admin 검토·재검토: 게시글 상태 변경 이력 기록 */
    @Transactional
    public void recordModeration(Long storyId, String action, Long actorId, String actorRole, Long reportId, String reasonCode, String previousStatus, String newStatus) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** admin 신고 목록 */
    public Page<PassStoryReport> getReportsForAdmin(String status, Pageable pageable) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** admin 신고 처리 결과 (CONFIRMED · DISMISSED) */
    @Transactional
    public void resolve(Long reportId, String status) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
