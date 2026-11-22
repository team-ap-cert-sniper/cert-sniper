package com.certsniper.targetexam.controller;

import com.certsniper.global.security.LoginUser;
import com.certsniper.targetexam.dto.TargetExamRequest;
import com.certsniper.targetexam.dto.TargetExamResponse;
import com.certsniper.targetexam.service.TargetExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "08. 목표 자격증")
@RestController
@RequestMapping("/api/v1/target-exams")
@RequiredArgsConstructor
public class TargetExamController {

    private final TargetExamService targetExamService;

    @Operation(summary = "목표 자격증 목록")
    @GetMapping
    public List<TargetExamResponse> getTargetExams(@LoginUser Long userId) {
        return targetExamService.getTargetExams(userId);
    }

    @Operation(summary = "목표 자격증 추가")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TargetExamResponse addTargetExam(
            @LoginUser Long userId,
            @Valid @RequestBody TargetExamRequest request) {
        return targetExamService.addTargetExam(userId, request);
    }

    @Operation(summary = "목표 자격증 수정 (시험일·색상)")
    @PatchMapping("/{targetExamId}")
    public TargetExamResponse updateTargetExam(
            @LoginUser Long userId,
            @Parameter(description = "목표 자격증 ID") @PathVariable Long targetExamId,
            @Valid @RequestBody TargetExamRequest request) {
        return targetExamService.updateTargetExam(userId, targetExamId, request);
    }

    @Operation(summary = "목표 자격증 삭제 (일정·메모 같이 삭제)")
    @DeleteMapping("/{targetExamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTargetExam(
            @LoginUser Long userId,
            @Parameter(description = "목표 자격증 ID") @PathVariable Long targetExamId) {
        targetExamService.deleteTargetExam(userId, targetExamId);
    }
}
