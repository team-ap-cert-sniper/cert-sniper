package com.certsniper.admin.service;

import com.certsniper.admin.dto.AdminReportResponse;
import com.certsniper.admin.dto.ReviewRequest;
import com.certsniper.global.common.PageResponse;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    public PageResponse<AdminReportResponse> getReports(String status, int page, int size) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void reviewReport(Long userId, Long reportId, ReviewRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void reReviewPassStory(Long userId, Long storyId, ReviewRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
