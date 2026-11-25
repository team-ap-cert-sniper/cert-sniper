package com.certsniper.qualification.service;

import com.certsniper.qualification.entity.Qualification;
import com.certsniper.qualification.repository.QualificationRepository;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * 자격증 Master(613종) 읽기 전용 목록. 서비스 중 바뀌지 않는 기준 데이터라 처음 조회할 때 한 번만 읽어 둔다.
 */
@Component
@RequiredArgsConstructor
public class QualificationCatalog {

    private static final Map<String, String> QUALGB_NAMES = Map.of("T", "국가기술자격", "S", "국가전문자격");

    private final QualificationRepository qualificationRepository;
    private volatile Map<String, Qualification> byJmcd;

    /** 종목코드 → 자격증 (종목코드 순) */
    public Map<String, Qualification> all() {
        Map<String, Qualification> loaded = byJmcd;
        if (loaded == null) {
            synchronized (this) {
                if (byJmcd == null) {
                    byJmcd = Collections.unmodifiableMap(qualificationRepository.findAll(Sort.by("jmcd")).stream()
                            .collect(Collectors.toMap(Qualification::getJmcd, Function.identity(), (a, b) -> a,
                                    LinkedHashMap::new)));
                }
                loaded = byJmcd;
            }
        }
        return loaded;
    }

    /** 자격구분명 (T 국가기술자격 · S 국가전문자격) */
    public static String qualgbnm(String qualgbcd) {
        return qualgbcd == null ? null : QUALGB_NAMES.get(qualgbcd);
    }
}
