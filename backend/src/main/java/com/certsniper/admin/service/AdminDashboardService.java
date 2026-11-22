package com.certsniper.admin.service;

import com.certsniper.admin.dto.DashboardResponse;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    public DashboardResponse getDashboard() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
