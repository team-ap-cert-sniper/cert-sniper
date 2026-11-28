package com.certsniper.memo.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.HtmlSanitizer;
import com.certsniper.memo.dto.MemoRequest;
import com.certsniper.memo.dto.MemoResponse;
import com.certsniper.memo.entity.Memo;
import com.certsniper.memo.repository.MemoRepository;
import com.certsniper.targetexam.service.TargetExamService;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 목표 자격증별 메모 (자격증마다 1개, 500자, 서식 도구) */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemoService {

    static final int MAX_TEXT = 500;
    /** content_html은 TEXT(65,535바이트) 칸이라 여유를 두고 막는다 */
    static final int MAX_HTML_BYTES = 60_000;

    private final MemoRepository memoRepository;
    private final TargetExamService targetExamService;

    /** 메모 조회: 아직 없거나 지운 메모면 빈 메모(version 0) */
    public MemoResponse getMemo(Long userId, Long targetExamId) {
        targetExamService.validateOwner(userId, targetExamId);
        return memoRepository.findByTargetExamId(targetExamId)
                .filter(memo -> !memo.getIsDeleted())
                .map(MemoService::toResponse)
                .orElse(new MemoResponse(null, targetExamId, "", "", 0));
    }

    /**
     * 메모 저장 (없으면 만들고, 있으면 덮어씀).
     * 서식은 허용 태그만 남기고, 글자 수는 서식을 뺀 글자로 센다. 다른 곳에서 먼저 저장했으면 409
     */
    @Transactional
    public MemoResponse saveMemo(Long userId, Long targetExamId, MemoRequest request) {
        targetExamService.validateOwner(userId, targetExamId);
        String html = HtmlSanitizer.sanitize(request.contentHtml());
        String text = HtmlSanitizer.toText(html).strip();
        if (text.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "메모 내용을 입력해 주세요.");
        }
        if (text.length() > MAX_TEXT) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "메모는 500자까지 쓸 수 있습니다.");
        }
        if (html.getBytes(StandardCharsets.UTF_8).length > MAX_HTML_BYTES) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "메모 서식이 너무 깁니다.");
        }

        Memo memo = memoRepository.findByTargetExamId(targetExamId).orElse(null);
        if (memo == null) {
            return toResponse(memoRepository.save(Memo.create(targetExamId, text, html)));
        }
        boolean changedElsewhere = !memo.getIsDeleted() && request.version() != null
                && !request.version().equals(memo.getVersion());
        if (changedElsewhere) {
            throw new BusinessException(ErrorCode.DUPLICATE, "메모가 다른 곳에서 먼저 수정되었습니다. 새로고침 후 다시 저장해 주세요.");
        }
        memo.update(text, html);
        return toResponse(memo);
    }

    /** 메모 삭제: 지운 표시만 한다 (없으면 아무 일도 안 함) */
    @Transactional
    public void deleteMemo(Long userId, Long targetExamId) {
        targetExamService.validateOwner(userId, targetExamId);
        memoRepository.findByTargetExamId(targetExamId).ifPresent(Memo::delete);
    }

    private static MemoResponse toResponse(Memo memo) {
        return new MemoResponse(memo.getId(), memo.getTargetExamId(), memo.getContentText(),
                memo.getContentHtml(), memo.getVersion());
    }
}
