package com.certsniper.support.repository;

import com.certsniper.support.entity.InquiryAnswer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {

    /** 문의당 답변 1건 (UQ inquiry_id) */
    Optional<InquiryAnswer> findByInquiryId(Long inquiryId);

    boolean existsByInquiryId(Long inquiryId);
}
