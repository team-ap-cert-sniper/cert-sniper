package com.certsniper.report.service;

import com.certsniper.global.common.PageResponse;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.passstory.entity.PassStory;
import com.certsniper.passstory.repository.PassStoryRepository;
import com.certsniper.passstory.service.PassStoryService;
import com.certsniper.report.dto.MyReportResponse;
import com.certsniper.report.dto.ReportRequest;
import com.certsniper.report.entity.PassStoryModerationLog;
import com.certsniper.report.entity.PassStoryReport;
import com.certsniper.report.repository.PassStoryModerationLogRepository;
import com.certsniper.report.repository.PassStoryReportRepository;
import java.util.Set;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final Set<String> RESOLVE_STATUSES = Set.of("CONFIRMED", "DISMISSED");

    private final PassStoryReportRepository passStoryReportRepository;
    private final PassStoryModerationLogRepository passStoryModerationLogRepository;
    private final PassStoryRepository passStoryRepository;
    private final PassStoryService passStoryService;

    // PassStoryService도 recordModeration 때문에 이 서비스를 받으므로 @Lazy로 순환 참조를 끊는다
    public ReportService(
            PassStoryReportRepository passStoryReportRepository,
            PassStoryModerationLogRepository passStoryModerationLogRepository,
            PassStoryRepository passStoryRepository,
            @Lazy PassStoryService passStoryService) {
        this.passStoryReportRepository = passStoryReportRepository;
        this.passStoryModerationLogRepository = passStoryModerationLogRepository;
        this.passStoryRepository = passStoryRepository;
        this.passStoryService = passStoryService;
    }

    /** 본인 글 FORBIDDEN, 같은 글 재신고 DUPLICATE. 공개 글이면 즉시 숨김 + 이력 */
    @Transactional
    public void reportPassStory(Long userId, Long storyId, ReportRequest request) {
        PassStory story = passStoryRepository.findById(storyId)
                .filter(s -> !"DELETED".equals(s.getStatus()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (story.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (passStoryReportRepository.existsByStoryIdAndReporterId(storyId, userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE);
        }
        PassStoryReport report = passStoryReportRepository.save(
                PassStoryReport.of(storyId, userId, request.reportType()));

        String previous = story.getStatus();
        passStoryService.hideByReport(storyId, request.reportType());
        if ("PUBLISHED".equals(previous)) {
            recordModeration(storyId, "REPORT_HIDE", userId, "REPORTER", report.getId(), request.reportType(),
                    previous, "HIDDEN");
        }
    }

    public PageResponse<MyReportResponse> getMyReports(Long userId, int page, int size) {
        // Phase 3 (마이페이지)
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** admin 대시보드: 미처리 신고 수 */
    public long countPending() {
        return passStoryReportRepository.countByStatus("PENDING");
    }

    /** passstory 수정·삭제, admin 검토·재검토: 게시글 상태 변경 이력 기록 */
    @Transactional
    public void recordModeration(Long storyId, String action, Long actorId, String actorRole, Long reportId, String reasonCode, String previousStatus, String newStatus) {
        passStoryModerationLogRepository.save(PassStoryModerationLog.of(
                storyId, action, actorId, actorRole, reportId, reasonCode, previousStatus, newStatus));
    }

    /** admin 신고 목록 (status가 null이면 전체) */
    public Page<PassStoryReport> getReportsForAdmin(String status, Pageable pageable) {
        return status == null
                ? passStoryReportRepository.findAll(pageable)
                : passStoryReportRepository.findByStatus(status, pageable);
    }

    /** admin 신고 처리 결과 (CONFIRMED · DISMISSED) */
    @Transactional
    public void resolve(Long reportId, String status) {
        if (!RESOLVE_STATUSES.contains(status)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        passStoryReportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND))
                .resolve(status);
    }
}
