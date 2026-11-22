package com.certsniper.support.service;

import com.certsniper.admin.dto.NoticeRequest;
import com.certsniper.global.common.PageResponse;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.support.dto.NoticeResponse;
import com.certsniper.support.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public PageResponse<NoticeResponse> getNotices(int page, int size) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    public NoticeResponse getNotice(Long noticeId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void createNotice(Long userId, NoticeRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void updateNotice(Long userId, Long noticeId, NoticeRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void deleteNotice(Long userId, Long noticeId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** admin 대시보드: 공지 수 */
    public long countNotices() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
