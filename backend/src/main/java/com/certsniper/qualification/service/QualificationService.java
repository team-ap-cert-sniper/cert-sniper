package com.certsniper.qualification.service;

import com.certsniper.examschedule.dto.ExamScheduleResponse;
import com.certsniper.examschedule.service.ExamScheduleService;
import com.certsniper.qualification.dto.EligibilityResponse;
import com.certsniper.qualification.dto.PassRateResponse;
import com.certsniper.qualification.dto.QualificationDetailResponse;
import com.certsniper.qualification.dto.QualificationDetailResponse.QualificationInfo;
import com.certsniper.qualification.entity.Qualification;
import com.certsniper.qualification.repository.QualificationEligibilityRuleRepository;
import com.certsniper.qualification.repository.QualificationStatisticsYearlyRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 자격증 상세 (etl service.qualification_detail과 같은 규칙) */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QualificationService {

    private static final String ELIGIBILITY_SOURCE_URL = "https://www.q-net.or.kr/crf006.do?id=crf00603";
    private static final String AVAILABLE = "AVAILABLE";
    private static final String NOT_ACQUIRED = "NOT_ACQUIRED";

    private final QualificationCatalog qualificationCatalog;
    private final QualificationStatisticsYearlyRepository qualificationStatisticsYearlyRepository;
    private final QualificationEligibilityRuleRepository qualificationEligibilityRuleRepository;
    private final ExamScheduleService examScheduleService;
    private final TrendService trendService;

    public QualificationDetailResponse getQualificationDetail(String jmcd) {
        Qualification q = qualificationCatalog.get(normalize(jmcd));
        List<ExamScheduleResponse> schedules = examScheduleService.getSchedules(q.getJmcd());
        Long nextScheduleId = schedules.stream()
                .filter(s -> ExamScheduleResponse.UPCOMING.equals(s.examStatus())
                        && !ExamScheduleResponse.CLOSED.equals(s.registrationStatus()))
                .map(ExamScheduleResponse::scheduleId)
                .findFirst()
                .orElse(null);
        List<PassRateResponse> passRates = qualificationStatisticsYearlyRepository
                .findByJmcdInOrderByIdAsc(List.of(q.getJmcd())).stream()
                .map(PassRateResponse::of)
                .sorted(Comparator.comparing(PassRateResponse::year)
                        .thenComparingInt(r -> PassRateResponse.stageOrder(r.examStage()))
                        .thenComparing(PassRateResponse::examStage))
                .toList();
        List<Short> years = passRates.stream().map(PassRateResponse::year).distinct()
                .sorted(Comparator.reverseOrder()).toList();
        TrendService.DetailTrend trend = trendService.detailTrend(q);
        return new QualificationDetailResponse(q.getJmcd(), toInfo(q), q.getGradeLevel(), eligibility(q),
                schedules, schedules.isEmpty() ? NOT_ACQUIRED : AVAILABLE, nextScheduleId,
                years, passRates, passRates.isEmpty() ? NOT_ACQUIRED : AVAILABLE,
                trend == null ? null : trend.item(), trend == null ? null : trend.typeTotal());
    }

    /** favorite·passstory·aiplanner: 자격증 기본정보 (없으면 NOT_FOUND) */
    public QualificationInfo getInfo(String jmcd) {
        return toInfo(qualificationCatalog.get(normalize(jmcd)));
    }

    /** admin 대시보드: 등록 자격증 수 */
    public long countQualifications() {
        return qualificationCatalog.all().size();
    }

    private QualificationInfo toInfo(Qualification q) {
        return new QualificationInfo(q.getJmcd(), q.getJmfldnm(), q.getEngName(), q.getQualgbcd(),
                QualificationCatalog.qualgbnm(q.getQualgbcd()), q.getGradeLevel(),
                q.getObligfldcd(), qualificationCatalog.interestFieldName(q.getObligfldcd()),
                q.getMdobligfldcd(), qualificationCatalog.jobCategoryName(q.getMdobligfldcd()),
                q.getRelatedMinistry(), q.getSummary(), q.getOverview(), q.getJobDescription(), q.getCareerOutlook(),
                q.getConductingAgency(), q.getRelatedMajors(), q.getExamMethod(), q.getExamSubjects(),
                q.getExamStandard(), q.getFeeWritten(), q.getFeePractical(), q.getFeeNote(), q.getScheduleType());
    }

    private EligibilityResponse eligibility(Qualification q) {
        List<EligibilityResponse.Condition> conditions = q.getEligibilityRuleKey() == null ? List.of()
                : qualificationEligibilityRuleRepository.findByRuleKeyOrderBySeqAsc(q.getEligibilityRuleKey()).stream()
                        .map(r -> new EligibilityResponse.Condition(r.getConditionType(), r.getConditionText()))
                        .toList();
        Boolean required = "Y".equals(q.getEligibilityRequired()) ? Boolean.TRUE
                : "N".equals(q.getEligibilityRequired()) ? Boolean.FALSE : null;
        return new EligibilityResponse(required, q.getEligibilitySummary(), conditions, ELIGIBILITY_SOURCE_URL);
    }

    /** 종목코드 표기 통일: 앞뒤 공백·'QNET:' 제거, 4자리 0 채움 */
    private static String normalize(String jmcd) {
        String code = jmcd == null ? "" : jmcd.trim().toUpperCase(Locale.ROOT);
        if (code.startsWith("QNET:")) {
            code = code.substring("QNET:".length());
        }
        return code.length() < 4 ? "0".repeat(4 - code.length()) + code : code;
    }
}
