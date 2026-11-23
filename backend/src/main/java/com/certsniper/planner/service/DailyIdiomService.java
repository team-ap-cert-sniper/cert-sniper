package com.certsniper.planner.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.DateUtils;
import com.certsniper.planner.dto.DailyIdiomResponse;
import com.certsniper.planner.entity.DailyIdiom;
import com.certsniper.planner.repository.DailyIdiomRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyIdiomService {

    private final DailyIdiomRepository dailyIdiomRepository;

    @Value("${app.idiom.base-date}")
    private LocalDate baseDate;

    /** 기준일에 1번, 다음 날 2번 … 하루 1개씩 순환 (etl daily_idiom과 같은 결과) */
    public DailyIdiomResponse getDailyIdiom() {
        List<DailyIdiom> idioms = dailyIdiomRepository.findAll(Sort.by("displayOrder"));
        if (idioms.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        LocalDate today = DateUtils.today();
        long days = ChronoUnit.DAYS.between(baseDate, today);
        int index = (int) Math.floorMod(days, (long) idioms.size());
        DailyIdiom idiom = idioms.get(index);
        return new DailyIdiomResponse(today, idiom.getDisplayOrder(), idiom.getIdiom(),
                idiom.getHanja(), idiom.getMeaning(), idioms.size());
    }
}