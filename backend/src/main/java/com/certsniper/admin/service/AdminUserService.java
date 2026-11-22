package com.certsniper.admin.service;

import com.certsniper.admin.dto.AdminUserDetailResponse;
import com.certsniper.admin.dto.ProfileCorrectionRequest;
import com.certsniper.admin.dto.SuspendRequest;
import com.certsniper.global.common.PageResponse;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    public PageResponse<AdminUserDetailResponse> getUsers(String keyword, String status, int page, int size) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    public AdminUserDetailResponse getUser(Long targetUserId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void correctUser(Long userId, Long targetUserId, ProfileCorrectionRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void suspendUser(Long userId, Long targetUserId, SuspendRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void unsuspendUser(Long userId, Long targetUserId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
