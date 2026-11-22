package com.certsniper.planner.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.planner.dto.DailyIdiomResponse;
import com.certsniper.planner.repository.DailyIdiomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyIdiomService {

    private final DailyIdiomRepository dailyIdiomRepository;

    public DailyIdiomResponse getDailyIdiom() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
