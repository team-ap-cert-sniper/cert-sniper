package com.certsniper.targetexam.repository;

import com.certsniper.targetexam.entity.TargetExam;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TargetExamRepository extends JpaRepository<TargetExam, Long> {

    long countByUserId(Long userId);

    List<TargetExam> findAllByUserIdOrderByIdAsc(Long userId);

    Optional<TargetExam> findByUserIdAndJmcd(Long userId, String jmcd);

    boolean existsByUserIdAndJmcd(Long userId, String jmcd);

    boolean existsByUserIdAndDisplayColor(Long userId, String displayColor);

    // 벌크 삭제: 일정바·메모·세부 일정은 DB의 ON DELETE CASCADE로 같이 삭제된다
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from TargetExam t where t.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
