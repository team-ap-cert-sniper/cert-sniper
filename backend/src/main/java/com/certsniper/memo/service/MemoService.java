package com.certsniper.memo.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.memo.dto.MemoRequest;
import com.certsniper.memo.dto.MemoResponse;
import com.certsniper.memo.repository.MemoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemoService {

    private final MemoRepository memoRepository;

    public MemoResponse getMemo(Long userId, Long targetExamId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public MemoResponse saveMemo(Long userId, Long targetExamId, MemoRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void deleteMemo(Long userId, Long targetExamId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
