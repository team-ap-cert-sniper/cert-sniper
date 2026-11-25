package com.certsniper.qualification.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.job.entity.InterestField;
import com.certsniper.job.entity.JobCategory;
import com.certsniper.job.repository.InterestFieldRepository;
import com.certsniper.job.repository.JobCategoryRepository;
import com.certsniper.qualification.entity.Qualification;
import com.certsniper.qualification.repository.QualificationRepository;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * 자격증 Master(613종)와 직무분야(대·중) 이름. 서비스 중 바뀌지 않는 기준 데이터라 처음 조회할 때 한 번만 읽어 둔다.
 */
@Component
@RequiredArgsConstructor
public class QualificationCatalog {

    private static final Map<String, String> QUALGB_NAMES = Map.of("T", "국가기술자격", "S", "국가전문자격");

    private final QualificationRepository qualificationRepository;
    private final InterestFieldRepository interestFieldRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private volatile Data data;

    private record Data(Map<String, Qualification> qualifications, Map<String, String> interestFields,
            Map<String, String> jobCategories) {}

    /** 종목코드 → 자격증 (종목코드 순) */
    public Map<String, Qualification> all() {
        return data().qualifications();
    }

    /** 종목코드로 찾기. 없으면 NOT_FOUND */
    public Qualification get(String jmcd) {
        Qualification q = jmcd == null ? null : all().get(jmcd);
        if (q == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "자격증을 찾을 수 없습니다.");
        }
        return q;
    }

    /** 관심 분야(직무분야 대분류) 이름 */
    public String interestFieldName(String code) {
        return code == null ? null : data().interestFields().get(code);
    }

    /** 희망 직무(직무분야 중분류) 이름 */
    public String jobCategoryName(String code) {
        return code == null ? null : data().jobCategories().get(code);
    }

    /** 보유 자격 종목코드 검증(중복 없음·있는 종목). 종목코드 순으로 돌려준다 */
    public List<Qualification> owned(List<String> jmcds) {
        List<String> codes = jmcds == null ? List.of() : jmcds;
        Map<String, Qualification> catalog = all();
        if (new HashSet<>(codes).size() != codes.size() || !codes.stream().allMatch(c -> c != null && catalog.containsKey(c))) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "보유 자격을 다시 확인해 주세요.");
        }
        return codes.stream().sorted().map(catalog::get).toList();
    }

    /** 자격구분명 (T 국가기술자격 · S 국가전문자격) */
    public static String qualgbnm(String qualgbcd) {
        return qualgbcd == null ? null : QUALGB_NAMES.get(qualgbcd);
    }

    private Data data() {
        Data loaded = data;
        if (loaded == null) {
            synchronized (this) {
                if (data == null) {
                    data = new Data(
                            Collections.unmodifiableMap(qualificationRepository.findAll(Sort.by("jmcd")).stream()
                                    .collect(Collectors.toMap(Qualification::getJmcd, Function.identity(), (a, b) -> a,
                                            LinkedHashMap::new))),
                            interestFieldRepository.findAll().stream()
                                    .collect(Collectors.toUnmodifiableMap(InterestField::getCode, InterestField::getName)),
                            jobCategoryRepository.findAll().stream()
                                    .collect(Collectors.toUnmodifiableMap(JobCategory::getCode, JobCategory::getName)));
                }
                loaded = data;
            }
        }
        return loaded;
    }
}
