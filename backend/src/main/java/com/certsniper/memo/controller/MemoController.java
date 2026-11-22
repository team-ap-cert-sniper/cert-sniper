package com.certsniper.memo.controller;

import com.certsniper.global.security.LoginUser;
import com.certsniper.memo.dto.MemoRequest;
import com.certsniper.memo.dto.MemoResponse;
import com.certsniper.memo.service.MemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "10. 메모")
@RestController
@RequestMapping("/api/v1/target-exams/{targetExamId}/memo")
@RequiredArgsConstructor
public class MemoController {

    private final MemoService memoService;

    @Operation(summary = "메모 조회")
    @GetMapping
    public MemoResponse getMemo(
            @LoginUser Long userId,
            @Parameter(description = "목표 자격증 ID") @PathVariable Long targetExamId) {
        return memoService.getMemo(userId, targetExamId);
    }

    @Operation(summary = "메모 저장")
    @PutMapping
    public MemoResponse saveMemo(
            @LoginUser Long userId,
            @Parameter(description = "목표 자격증 ID") @PathVariable Long targetExamId,
            @Valid @RequestBody MemoRequest request) {
        return memoService.saveMemo(userId, targetExamId, request);
    }

    @Operation(summary = "메모 삭제")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMemo(
            @LoginUser Long userId,
            @Parameter(description = "목표 자격증 ID") @PathVariable Long targetExamId) {
        memoService.deleteMemo(userId, targetExamId);
    }
}
