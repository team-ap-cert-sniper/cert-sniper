package com.certsniper.support.controller;

import com.certsniper.global.common.PageResponse;
import com.certsniper.support.dto.NoticeResponse;
import com.certsniper.support.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "15. 고객센터")
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지사항 목록")
    @GetMapping
    public PageResponse<NoticeResponse> getNotices(
            @Parameter(description = "페이지 (0부터)") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "10") int size) {
        return noticeService.getNotices(page, size);
    }

    @Operation(summary = "공지사항 상세")
    @GetMapping("/{noticeId}")
    public NoticeResponse getNotice(@Parameter(description = "공지 ID") @PathVariable Long noticeId) {
        return noticeService.getNotice(noticeId);
    }
}
