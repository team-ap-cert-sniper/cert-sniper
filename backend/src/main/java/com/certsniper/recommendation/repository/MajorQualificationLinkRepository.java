package com.certsniper.recommendation.repository;

import com.certsniper.recommendation.entity.MajorQualificationLink;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MajorQualificationLinkRepository extends JpaRepository<MajorQualificationLink, Long> {

    /** 학과별 자격증 연결 (연결 순위 순) */
    List<MajorQualificationLink> findByMajorIdInOrderByLinkRankAscIdAsc(Collection<Long> majorIds);
}
