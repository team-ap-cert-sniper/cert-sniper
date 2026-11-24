package com.certsniper.support.repository;

import com.certsniper.support.entity.Inquiry;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    /** 나의 문의 목록: 내용(content) 없이 필요한 컬럼만, 최신순 (IDX user_id, created_at) */
    Page<ListRow> findByUserIdOrderByCreatedAtDescIdDesc(Long userId, Pageable pageable);

    /** 관리자 문의 목록: 상태·회원 조건은 값이 있을 때만 적용 */
    @Query(value = """
            select i.id as id, i.userId as userId, i.inquiryType as inquiryType,
                   i.title as title, i.status as status, i.createdAt as createdAt
            from Inquiry i
            where (:status is null or i.status = :status)
              and (:userId is null or i.userId = :userId)
            order by i.createdAt desc, i.id desc
            """,
            countQuery = """
            select count(i) from Inquiry i
            where (:status is null or i.status = :status)
              and (:userId is null or i.userId = :userId)
            """)
    Page<ListRow> searchForAdmin(@Param("status") String status, @Param("userId") Long userId, Pageable pageable);

    long countByStatus(String status);

    /** 목록 조회용 컬럼 */
    interface ListRow {
        Long getId();

        Long getUserId();

        String getInquiryType();

        String getTitle();

        String getStatus();

        LocalDateTime getCreatedAt();
    }
}
