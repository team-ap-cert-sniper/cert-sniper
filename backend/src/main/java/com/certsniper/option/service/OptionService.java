package com.certsniper.option.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.job.entity.InterestField;
import com.certsniper.job.entity.JobCategory;
import com.certsniper.job.repository.InterestFieldRepository;
import com.certsniper.job.repository.JobCategoryRepository;
import com.certsniper.option.dto.OptionItem;
import com.certsniper.option.dto.OptionResponse;
import com.certsniper.option.dto.SchoolInfoResponse;
import com.certsniper.qualification.entity.Qualification;
import com.certsniper.qualification.repository.QualificationRepository;
import com.certsniper.qualification.repository.QualificationRepository.FieldCount;
import com.certsniper.qualification.service.QualificationCatalog;
import com.certsniper.university.entity.College;
import com.certsniper.university.entity.Major;
import com.certsniper.university.entity.University;
import com.certsniper.university.repository.CollegeRepository;
import com.certsniper.university.repository.MajorRepository;
import com.certsniper.university.repository.UniversityRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OptionService {

    private static final String SCOPE_MAJOR = "MAJOR";
    private static final String SCOPE_FACULTY = "FACULTY";
    private static final String SCHOOL_MESSAGE = "학교 정보를 다시 확인해 주세요.";

    /** 학년 목록 (users.grade CHECK 값과 같음) */
    private static final Map<String, String> GRADES = new LinkedHashMap<>();

    static {
        for (int i = 1; i <= 6; i++) {
            GRADES.put(String.valueOf(i), i + "학년");
        }
        GRADES.put("GRADUATED", "졸업");
        GRADES.put("NOT_ENROLLED", "비재학");
    }

    private final UniversityRepository universityRepository;
    private final CollegeRepository collegeRepository;
    private final MajorRepository majorRepository;
    private final QualificationRepository qualificationRepository;
    private final QualificationCatalog qualificationCatalog;
    private final InterestFieldRepository interestFieldRepository;
    private final JobCategoryRepository jobCategoryRepository;

    public OptionResponse getOptions(
            String kind,
            Long universityId,
            String query,
            int offset,
            int limit,
            String qualgbcd,
            String gradeLevel,
            String interestFieldCode,
            String jobCategoryCode) {
        if (offset < 0 || limit < 1 || limit > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "offset은 0 이상, limit은 1~100이어야 합니다.");
        }
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

        List<OptionItem> items = switch (kind == null ? "" : kind) {
            case "universities" -> universities(q);
            case "majors" -> majors(universityId, q);
            case "grades" -> grades(q);
            case "qualifications" -> qualifications(q, qualgbcd, gradeLevel, interestFieldCode, jobCategoryCode);
            case "interest_fields" -> fields(q, qualgbcd, null, true);
            case "job_categories" -> fields(q, qualgbcd, interestFieldCode, false);
            default -> throw new BusinessException(ErrorCode.VALIDATION_ERROR, "지원하지 않는 목록 종류입니다.");
        };
        return page(kind, items, offset, limit);
    }

    /** auth·user: 대학·학부·전공·학년 묶음 검증 (셋 다 입력 또는 셋 다 비움) */
    public void validateSchoolInfo(Long universityId, Long collegeId, Long majorId, String grade) {
        boolean gradeBlank = grade == null || grade.isBlank();
        if (universityId == null && collegeId == null && majorId == null && gradeBlank) {
            return; // 학교 정보 없음
        }
        if (universityId == null || gradeBlank || (collegeId == null && majorId == null)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "대학교·전공·학년을 모두 선택해 주세요.");
        }
        if (!GRADES.containsKey(grade)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, SCHOOL_MESSAGE);
        }
        if (!universityRepository.existsById(universityId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, SCHOOL_MESSAGE);
        }
        if (collegeId != null) {
            College college = collegeRepository.findById(collegeId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, SCHOOL_MESSAGE));
            if (!college.getUniversityId().equals(universityId)) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, SCHOOL_MESSAGE);
            }
        }
        if (majorId != null) {
            Major major = majorRepository.findById(majorId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, SCHOOL_MESSAGE));
            if (!major.getUniversityId().equals(universityId)) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, SCHOOL_MESSAGE);
            }
            if (collegeId != null && major.getCollegeId() != null && !major.getCollegeId().equals(collegeId)) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, SCHOOL_MESSAGE);
            }
        }
    }

    /** user 회원정보: 대학·학부·학과 표시명 */
    public SchoolInfoResponse getSchoolNames(Long universityId, Long collegeId, Long majorId) {
        String universityName = universityId == null ? null
                : universityRepository.findById(universityId).map(OptionService::universityLabel).orElse(null);
        String collegeName = collegeId == null ? null
                : collegeRepository.findById(collegeId).map(College::getName).orElse(null);
        String majorName = majorId == null ? null
                : majorRepository.findById(majorId).map(Major::getName).orElse(null);
        return new SchoolInfoResponse(universityName, collegeName, majorName);
    }

    /* ---------- 목록별 조회 ---------- */

    private List<OptionItem> universities(String q) {
        return universityRepository.findAll().stream()
                .filter(u -> contains(u.getName(), q) || contains(u.getCampusName(), q))
                .sorted(Comparator.comparing(University::getName).thenComparing(University::getCampusName))
                .map(u -> item(String.valueOf(u.getId()), u.getName(), u.getCampusName(), u.getSchoolType(),
                        null, null, universityLabel(u)))
                .toList();
    }

    /**
     * 그 학교 전공 + '(학부 전체)' 항목.
     * FACULTY 항목의 id는 학부 ID(colleges.id) — 가입 시 college_id로 보낸다.
     * 학부 이름과 같은 전공 행(aggregate_college_id 있음)과 is_list_item 학부가 FACULTY 항목이 된다.
     */
    private List<OptionItem> majors(Long universityId, String q) {
        if (universityId == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "university_id가 필요합니다.");
        }
        List<Sortable> rows = new ArrayList<>();
        for (Major m : majorRepository.findByUniversityId(universityId)) {
            if (m.getAggregateCollegeId() != null) {
                rows.add(new Sortable(m.getName(), 0, faculty(m.getAggregateCollegeId(), m.getName(), m.getParentName())));
            } else {
                String group = m.getParentName() != null ? m.getParentName() : m.getName();
                String label = m.getParentName() != null ? m.getName() + " · " + m.getParentName() : m.getName();
                rows.add(new Sortable(group, 1, item(String.valueOf(m.getId()), m.getName(), null, null,
                        m.getParentName(), SCOPE_MAJOR, label)));
            }
        }
        for (College c : collegeRepository.findByUniversityIdAndIsListItemTrue(universityId)) {
            rows.add(new Sortable(c.getName(), 0, faculty(c.getId(), c.getName(), null)));
        }
        Predicate<OptionItem> match = i -> contains(i.name(), q) || contains(i.parentName(), q);
        return rows.stream()
                .filter(r -> match.test(r.item()))
                .sorted(Comparator.comparing(Sortable::group).thenComparingInt(Sortable::order)
                        .thenComparing(r -> r.item().name()))
                .map(Sortable::item)
                .toList();
    }

    private List<OptionItem> grades(String q) {
        return GRADES.entrySet().stream()
                .filter(e -> contains(e.getValue(), q))
                .map(e -> item(e.getKey(), e.getValue(), null, null, null, null, null))
                .toList();
    }

    private List<OptionItem> qualifications(
            String q, String qualgbcd, String gradeLevel, String interestFieldCode, String jobCategoryCode) {
        requireQualgbcd(qualgbcd);
        if ("S".equals(qualgbcd) && gradeLevel != null && !gradeLevel.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "국가전문자격(S)은 등급으로 거를 수 없습니다.");
        }
        return qualificationCatalog.all().values().stream()
                .filter(x -> isBlank(qualgbcd) || qualgbcd.equals(x.getQualgbcd()))
                .filter(x -> isBlank(gradeLevel) || gradeLevel.equals(x.getGradeLevel()))
                .filter(x -> isBlank(interestFieldCode) || interestFieldCode.equals(x.getObligfldcd()))
                .filter(x -> isBlank(jobCategoryCode) || jobCategoryCode.equals(x.getMdobligfldcd()))
                .filter(x -> contains(x.getJmfldnm(), q))
                .sorted(Comparator.comparing(Qualification::getJmfldnm))
                .map(x -> new OptionItem(null, null, null, null, null, null, null, null, null,
                        x.getJmcd(), x.getJmfldnm(), x.getQualgbcd(), QualificationCatalog.qualgbnm(x.getQualgbcd()), x.getGradeLevel()))
                .toList();
    }

    /**
     * 직무분야 목록. large = 대분류(interest_fields), 아니면 중분류(job_categories).
     * count = 조건에 맞는 종목 수, 종목이 0개인 분야는 제외. 이름 → 코드 순.
     */
    private List<OptionItem> fields(String q, String qualgbcd, String interestFieldCode, boolean large) {
        requireQualgbcd(qualgbcd);
        Function<FieldCount, String> key = large ? FieldCount::getObligfldcd : FieldCount::getMdobligfldcd;
        Map<String, Long> counts = qualificationRepository.countByField(isBlank(qualgbcd) ? null : qualgbcd).stream()
                .filter(c -> key.apply(c) != null)
                .filter(c -> large || isBlank(interestFieldCode) || interestFieldCode.equals(c.getObligfldcd()))
                .collect(Collectors.groupingBy(key, Collectors.summingLong(FieldCount::getTotal)));
        Map<String, String> names = large
                ? interestFieldRepository.findAll().stream()
                        .collect(Collectors.toMap(InterestField::getCode, InterestField::getName))
                : jobCategoryRepository.findAll().stream()
                        .collect(Collectors.toMap(JobCategory::getCode, JobCategory::getName));
        return names.entrySet().stream()
                .filter(e -> counts.containsKey(e.getKey()) && contains(e.getValue(), q))
                .sorted(Map.Entry.<String, String>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .map(e -> new OptionItem(null, e.getKey(), e.getValue(), null, null, null, null, null,
                        counts.get(e.getKey()).intValue(), null, null, null, null, null))
                .toList();
    }

    /* ---------- 도우미 ---------- */

    private static void requireQualgbcd(String qualgbcd) {
        if (!isBlank(qualgbcd) && !"T".equals(qualgbcd) && !"S".equals(qualgbcd)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "qualgbcd는 T 또는 S만 가능합니다.");
        }
    }

    private static OptionResponse page(String kind, List<OptionItem> items, int offset, int limit) {
        int from = Math.min(offset, items.size());
        int to = Math.min(from + limit, items.size());
        return new OptionResponse(kind, items.subList(from, to), items.size(), offset, limit);
    }

    private static OptionItem faculty(Long collegeId, String name, String parentName) {
        return item(String.valueOf(collegeId), name, null, null, parentName, SCOPE_FACULTY, name + " (학부 전체)");
    }

    private static OptionItem item(String id, String name, String campusName, String schoolType,
            String parentName, String scope, String label) {
        return new OptionItem(id, null, name, campusName, schoolType, parentName, scope, label,
                null, null, null, null, null, null);
    }

    private static String universityLabel(University u) {
        return u.getCampusName() == null || u.getCampusName().isBlank()
                ? u.getName() : u.getName() + " (" + u.getCampusName() + ")";
    }

    private static boolean contains(String value, String q) {
        return q.isEmpty() || (value != null && value.toLowerCase(Locale.ROOT).contains(q));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** 학부별로 묶고, 묶음 안에서 학부 항목(0) → 전공(1) 순서로 정렬하기 위한 행 */
    private record Sortable(String group, int order, OptionItem item) {}
}