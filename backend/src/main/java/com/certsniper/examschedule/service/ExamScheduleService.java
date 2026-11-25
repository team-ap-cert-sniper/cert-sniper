package com.certsniper.examschedule.service;

import com.certsniper.examschedule.dto.DdayResponse;
import com.certsniper.examschedule.dto.ExamScheduleResponse;
import com.certsniper.examschedule.entity.QualificationExamSchedule;
import com.certsniper.examschedule.repository.QualificationExamScheduleRepository;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.DateUtils;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamScheduleService {

    private final QualificationExamScheduleRepository qualificationExamScheduleRepository;
    private final DdayCalculator ddayCalculator;

    /** 시험 D-Day. 시험 기간이 여러 날이면 실제 응시일(examDate)이 필요하다 */
    public DdayResponse getDday(Long scheduleId, LocalDate examDate) {
        QualificationExamSchedule s = qualificationExamScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "시험 일정을 찾을 수 없습니다."));
        if (examDate == null && !s.getExamStartDate().equals(s.getExamEndDate())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "시험 기간이 여러 날이면 응시일을 골라 주세요.");
        }
        LocalDate chosen = examDate == null ? s.getExamStartDate() : examDate;
        if (chosen.isBefore(s.getExamStartDate()) || chosen.isAfter(s.getExamEndDate())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "응시일이 시험 기간 밖입니다.");
        }
        LocalDate today = DateUtils.today();
        long days = ddayCalculator.daysRemaining(chosen, today);
        return new DdayResponse(s.getId(), s.getJmcd(), s.getExamStage(), chosen, today, days,
                ddayCalculator.label(chosen, today), days > 0 ? "UPCOMING" : days == 0 ? "TODAY" : "ENDED");
    }

    /** qualification·targetexam·aiplanner: 종목의 시험 회차 목록 (진행·예정 회차는 가까운 순, 끝난 회차는 최근 순으로 뒤에) */
    public List<ExamScheduleResponse> getSchedules(String jmcd) {
        LocalDate today = DateUtils.today();
        List<ExamScheduleResponse> all = qualificationExamScheduleRepository
                .findByJmcdInOrderByExamStartDateAscIdAsc(List.of(jmcd)).stream()
                .map(s -> ExamScheduleResponse.of(s, today))
                .toList();
        Stream<ExamScheduleResponse> active = all.stream().filter(s -> !ExamScheduleResponse.ENDED.equals(s.examStatus()));
        Stream<ExamScheduleResponse> ended = all.stream()
                .filter(s -> ExamScheduleResponse.ENDED.equals(s.examStatus()))
                .sorted(Comparator.comparing(ExamScheduleResponse::examStartDate).reversed());
        return Stream.concat(active, ended).toList();
    }
}
