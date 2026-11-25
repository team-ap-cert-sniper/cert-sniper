package com.certsniper.support.service;

import com.certsniper.admin.dto.NoticeRequest;
import com.certsniper.admin.service.AuditLogService;
import com.certsniper.global.common.PageResponse;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.HtmlSanitizer;
import com.certsniper.support.dto.NoticeResponse;
import com.certsniper.support.entity.Notice;
import com.certsniper.support.repository.NoticeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    /** notices.notice_type CHECK 값 */
    private static final Set<String> NOTICE_TYPES = Set.of("GENERAL", "MAINTENANCE");
    private static final int MAX_PAGE_SIZE = 100;
    /** notices.content(text) 최대 크기. 넘으면 DB 오류(500) 대신 입력 오류로 돌려줌 */
    private static final int MAX_CONTENT_BYTES = 65_535;
    private static final String TARGET_TABLE = "notices";

    private final NoticeRepository noticeRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    /** 공지 목록: 본문은 상세에서만 (content = null) */
    public PageResponse<NoticeResponse> getNotices(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return PageResponse.of(noticeRepository
                .findByIsDeletedFalseOrderByCreatedAtDescIdDesc(PageRequest.of(page, size))
                .map(row -> new NoticeResponse(row.getId(), row.getNoticeType(), row.getTitle(), null, row.getCreatedAt())));
    }

    public NoticeResponse getNotice(Long noticeId) {
        Notice notice = findActive(noticeId);
        return new NoticeResponse(notice.getId(), notice.getNoticeType(), notice.getTitle(), notice.getContent(), notice.getCreatedAt());
    }

    @Transactional
    public void createNotice(Long userId, NoticeRequest request) {
        String content = sanitizeContent(request.content());
        validateNoticeType(request.noticeType());
        Notice notice = noticeRepository.save(new Notice(userId, request.noticeType(), request.title(), content));
        auditLogService.record(userId, null, "CREATE", TARGET_TABLE, String.valueOf(notice.getId()), null, toJson(notice), null);
    }

    @Transactional
    public void updateNotice(Long userId, Long noticeId, NoticeRequest request) {
        String content = sanitizeContent(request.content());
        validateNoticeType(request.noticeType());
        Notice notice = findActive(noticeId);
        String beforeJson = toJson(notice);
        notice.update(request.noticeType(), request.title(), content);
        auditLogService.record(userId, null, "UPDATE", TARGET_TABLE, String.valueOf(noticeId), beforeJson, toJson(notice), null);
    }

    @Transactional
    public void deleteNotice(Long userId, Long noticeId) {
        Notice notice = findActive(noticeId);
        String beforeJson = toJson(notice);
        notice.delete();
        auditLogService.record(userId, null, "DELETE", TARGET_TABLE, String.valueOf(noticeId), beforeJson, null, null);
    }

    /** admin 대시보드: 공지 수 */
    public long countNotices() {
        return noticeRepository.countByIsDeletedFalse();
    }

    /** 삭제된 공지는 없는 것으로 취급 */
    private Notice findActive(Long noticeId) {
        return noticeRepository.findByIdAndIsDeletedFalse(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private void validateNoticeType(String noticeType) {
        if (!NOTICE_TYPES.contains(noticeType)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    /** 저장 전 XSS 필터링. 허용 태그만 남긴 뒤 글자가 없거나 text 컬럼 크기를 넘으면 입력 오류 */
    private String sanitizeContent(String content) {
        String sanitized = HtmlSanitizer.sanitize(content);
        if (sanitized == null || HtmlSanitizer.toText(sanitized).isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (sanitized.getBytes(StandardCharsets.UTF_8).length > MAX_CONTENT_BYTES) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "공지 본문이 너무 깁니다.");
        }
        return sanitized;
    }

    private String toJson(Notice notice) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("notice_type", notice.getNoticeType());
        values.put("title", notice.getTitle());
        values.put("content", notice.getContent());
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
