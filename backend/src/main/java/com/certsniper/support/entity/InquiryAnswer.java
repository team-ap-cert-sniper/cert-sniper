package com.certsniper.support.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 문의 답변 (inquiry_answers) */
@Entity
@Table(name = "inquiry_answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "inquiry_id", nullable = false)
    private Long inquiryId;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    public InquiryAnswer(Long inquiryId, Long adminId, String content) {
        this.inquiryId = inquiryId;
        this.adminId = adminId;
        this.content = content;
    }
}
