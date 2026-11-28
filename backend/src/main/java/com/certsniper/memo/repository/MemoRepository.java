package com.certsniper.memo.repository;

import com.certsniper.memo.entity.Memo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoRepository extends JpaRepository<Memo, Long> {

    /** 목표 자격증마다 메모는 1개 (지운 표시된 것 포함) */
    Optional<Memo> findByTargetExamId(Long targetExamId);
}
