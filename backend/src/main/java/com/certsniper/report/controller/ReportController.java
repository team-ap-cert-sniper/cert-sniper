package com.certsniper.report.controller;

import com.certsniper.global.common.PageResponse;
import com.certsniper.global.security.LoginUser;
import com.certsniper.report.dto.MyReportResponse;
import com.certsniper.report.dto.ReportRequest;
import com.certsniper.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "13. 신고")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "합격수기 신고 (즉시 숨김)")
    @PostMapping("/pass-stories/{storyId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public void reportPassStory(
            @LoginUser Long userId,
            @Parameter(description = "게시글 ID") @PathVariable Long storyId,
            @Valid @RequestBody ReportRequest request) {
        reportService.reportPassStory(userId, storyId, request);
    }

    @Operation(summary = "내 글 신고 내역")
    @GetMapping("/users/me/reports")
    public PageResponse<MyReportResponse> getMyReports(
            @LoginUser Long userId,
            @Parameter(description = "페이지 (0부터)") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "10") int size) {
        return reportService.getMyReports(userId, page, size);
    }
}
