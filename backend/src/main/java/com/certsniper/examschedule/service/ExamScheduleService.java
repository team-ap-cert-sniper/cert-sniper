package com.certsniper.examschedule.service;

import com.certsniper.examschedule.dto.DdayResponse;
import com.certsniper.examschedule.dto.ExamScheduleResponse;
import com.certsniper.examschedule.repository.QualificationExamScheduleRepository;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamScheduleService {

    private final QualificationExamScheduleRepository qualificationExamScheduleRepository;

    public DdayResponse getDday(Long scheduleId, LocalDate examDate) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** qualification·targetexam·aiplanner: 종목의 시험 회차 목록 */
    public List<ExamScheduleResponse> getSchedules(String jmcd) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
