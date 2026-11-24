package com.certsniper.support.entity;

import com.certsniper.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 1:1 문의 (inquiries) */
@Entity
@Table(name = "inquiries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "inquiry_type", nullable = false, length = 20)
    private String inquiryType;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "WAITING";

    public Inquiry(Long userId, String inquiryType, String title, String content) {
        this.userId = userId;
        this.inquiryType = inquiryType;
        this.title = title;
        this.content = content;
    }

    /** 답변 저장 시 답변 완료로 변경 */
    public void markAnswered() {
        this.status = "ANSWERED";
    }
}
