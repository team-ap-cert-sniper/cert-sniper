package com.certsniper.support.service;

import com.certsniper.admin.dto.InquiryAnswerRequest;
import com.certsniper.global.common.PageResponse;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.support.dto.InquiryRequest;
import com.certsniper.support.dto.InquiryResponse;
import com.certsniper.support.repository.InquiryAnswerRepository;
import com.certsniper.support.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;

    @Transactional
    public void createInquiry(Long userId, InquiryRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    public PageResponse<InquiryResponse> getMyInquiries(Long userId, int page, int size) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    public InquiryResponse getMyInquiry(Long userId, Long inquiryId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    public PageResponse<InquiryResponse> getInquiriesForAdmin(String status, Long targetUserId, int page, int size) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void answerInquiry(Long userId, Long inquiryId, InquiryAnswerRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** admin 대시보드: 미답변 문의 수 */
    public long countUnanswered() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
