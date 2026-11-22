package com.certsniper.admin.controller;

import com.certsniper.admin.dto.AdminReportResponse;
import com.certsniper.admin.dto.ReviewRequest;
import com.certsniper.admin.service.AdminReportService;
import com.certsniper.global.common.PageResponse;
import com.certsniper.global.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "16. 관리자")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @Operation(summary = "신고 목록")
    @GetMapping("/reports")
    public PageResponse<AdminReportResponse> getReports(
            @Parameter(description = "상태") @RequestParam(name = "status", required = false) String status,
            @Parameter(description = "페이지 (0부터)") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "10") int size) {
        return adminReportService.getReports(status, page, size);
    }

    @Operation(summary = "신고 검토 (숨김 확정·해제)")
    @PatchMapping("/reports/{reportId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reviewReport(
            @LoginUser Long userId,
            @Parameter(description = "신고 ID") @PathVariable Long reportId,
            @Valid @RequestBody ReviewRequest request) {
        adminReportService.reviewReport(userId, reportId, request);
    }

    @Operation(summary = "재검토 (숨김 해제·삭제)")
    @PatchMapping("/pass-stories/{storyId}/re-review")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reReviewPassStory(
            @LoginUser Long userId,
            @Parameter(description = "게시글 ID") @PathVariable Long storyId,
            @Valid @RequestBody ReviewRequest request) {
        adminReportService.reReviewPassStory(userId, storyId, request);
    }
}
