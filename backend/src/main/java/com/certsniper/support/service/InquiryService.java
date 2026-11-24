package com.certsniper.support.service;

import com.certsniper.admin.dto.InquiryAnswerRequest;
import com.certsniper.admin.service.AuditLogService;
import com.certsniper.global.common.PageResponse;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.support.dto.InquiryRequest;
import com.certsniper.support.dto.InquiryResponse;
import com.certsniper.support.entity.Inquiry;
import com.certsniper.support.entity.InquiryAnswer;
import com.certsniper.support.enums.InquiryType;
import com.certsniper.support.repository.InquiryAnswerRepository;
import com.certsniper.support.repository.InquiryRepository;
import com.certsniper.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private static final String WAITING = "WAITING";
    private static final String ANSWERED = "ANSWERED";
    /** inquiries.status CHECK 값 */
    private static final Set<String> STATUSES = Set.of(WAITING, ANSWERED);
    private static final int MAX_PAGE_SIZE = 100;

    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createInquiry(Long userId, InquiryRequest request) {
        inquiryRepository.save(new Inquiry(userId, request.inquiryType().name(), request.title(), request.content()));
    }

    /** 나의 문의 목록: 내용·답변은 상세에서만. 본인 목록이라 닉네임은 채우지 않음 */
    public PageResponse<InquiryResponse> getMyInquiries(Long userId, int page, int size) {
        Page<InquiryRepository.ListRow> rows =
                inquiryRepository.findByUserIdOrderByCreatedAtDescIdDesc(userId, pageRequest(page, size));
        return PageResponse.of(rows.map(row -> toListResponse(row, null)));
    }

    /** 다른 회원의 문의는 403 (Q11 팀 확인 중) */
    public InquiryResponse getMyInquiry(Long userId, Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!inquiry.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        String answer = inquiryAnswerRepository.findByInquiryId(inquiryId)
                .map(InquiryAnswer::getContent)
                .orElse(null);
        return new InquiryResponse(inquiry.getId(), inquiry.getUserId(), null,
                InquiryType.valueOf(inquiry.getInquiryType()), inquiry.getTitle(), inquiry.getContent(),
                inquiry.getStatus(), answer, inquiry.getCreatedAt());
    }

    /** 관리자 문의 목록: 상태·회원 조건은 값이 있을 때만. 닉네임은 한 번에 조회 */
    public PageResponse<InquiryResponse> getInquiriesForAdmin(String status, Long targetUserId, int page, int size) {
        String statusFilter = (status == null || status.isBlank()) ? null : status;
        if (statusFilter != null && !STATUSES.contains(statusFilter)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Page<InquiryRepository.ListRow> rows =
                inquiryRepository.searchForAdmin(statusFilter, targetUserId, pageRequest(page, size));
        if (rows.isEmpty()) {
            return PageResponse.of(rows.map(row -> toListResponse(row, null)));
        }
        Map<Long, String> nicknames = userService.getNicknames(
                rows.getContent().stream().map(InquiryRepository.ListRow::getUserId).collect(Collectors.toSet()));
        return PageResponse.of(rows.map(row -> toListResponse(row, nicknames.get(row.getUserId()))));
    }

    /** 문의당 답변 1건. 저장과 함께 답변 완료로 변경 */
    @Transactional
    public void answerInquiry(Long userId, Long inquiryId, InquiryAnswerRequest request) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (ANSWERED.equals(inquiry.getStatus()) || inquiryAnswerRepository.existsByInquiryId(inquiryId)) {
            throw new BusinessException(ErrorCode.DUPLICATE);
        }
        InquiryAnswer answer;
        try {
            answer = inquiryAnswerRepository.saveAndFlush(new InquiryAnswer(inquiryId, userId, request.content()));
        } catch (DataIntegrityViolationException e) {
            // 동시에 답변한 경우 UQ(inquiry_id) 위반
            throw new BusinessException(ErrorCode.DUPLICATE);
        }
        inquiry.markAnswered();
        auditLogService.record(userId, inquiry.getUserId(), "ANSWER", "inquiry_answers",
                String.valueOf(answer.getId()), null, toJson(answer), null);
    }

    /** admin 대시보드: 미답변 문의 수 */
    public long countUnanswered() {
        return inquiryRepository.countByStatus(WAITING);
    }

    private static PageRequest pageRequest(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return PageRequest.of(page, size);
    }

    private static InquiryResponse toListResponse(InquiryRepository.ListRow row, String nickname) {
        return new InquiryResponse(row.getId(), row.getUserId(), nickname, InquiryType.valueOf(row.getInquiryType()),
                row.getTitle(), null, row.getStatus(), null, row.getCreatedAt());
    }

    private String toJson(InquiryAnswer answer) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("inquiry_id", answer.getInquiryId());
        values.put("content", answer.getContent());
        values.put("status", ANSWERED);
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
