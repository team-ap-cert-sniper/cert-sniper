package com.certsniper.support.repository;

import com.certsniper.support.entity.Notice;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /** 공지 목록: 본문(content) 없이 필요한 컬럼만, 최신순 (IDX is_deleted, created_at) */
    Page<ListRow> findByIsDeletedFalseOrderByCreatedAtDescIdDesc(Pageable pageable);

    Optional<Notice> findByIdAndIsDeletedFalse(Long id);

    long countByIsDeletedFalse();

    /** 목록 조회용 컬럼 */
    interface ListRow {
        Long getId();

        String getNoticeType();

        String getTitle();

        LocalDateTime getCreatedAt();
    }
}
