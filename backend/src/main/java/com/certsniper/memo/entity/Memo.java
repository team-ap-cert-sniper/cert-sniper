package com.certsniper.memo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 자격증별 메모 (memos) */
@Entity
@Table(name = "memos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Memo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "target_exam_id", nullable = false)
    private Long targetExamId;

    @Column(name = "content_text", nullable = false, length = 500)
    private String contentText;

    @Column(name = "content_html", nullable = false, columnDefinition = "text")
    private String contentHtml;

    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /** 메모 생성 */
    public static Memo create(Long targetExamId, String contentText, String contentHtml) {
        Memo memo = new Memo();
        memo.targetExamId = targetExamId;
        memo.contentText = contentText;
        memo.contentHtml = contentHtml;
        return memo;
    }

    /** 내용 덮어쓰기: 버전을 1 올리고, 지운 메모였으면 되살린다 */
    public void update(String contentText, String contentHtml) {
        this.contentText = contentText;
        this.contentHtml = contentHtml;
        this.version = this.version + 1;
        this.isDeleted = false;
    }

    /** 삭제 표시 (같은 자격증에 다시 쓰면 되살아난다) */
    public void delete() {
        this.isDeleted = true;
    }
}
