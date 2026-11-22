package com.certsniper.admin.controller;

import com.certsniper.admin.dto.NoticeRequest;
import com.certsniper.global.security.LoginUser;
import com.certsniper.support.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "16. 관리자")
@RestController
@RequestMapping("/api/v1/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createNotice(
            @LoginUser Long userId,
            @Valid @RequestBody NoticeRequest request) {
        noticeService.createNotice(userId, request);
    }

    @Operation(summary = "공지 수정")
    @PutMapping("/{noticeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateNotice(
            @LoginUser Long userId,
            @Parameter(description = "공지 ID") @PathVariable Long noticeId,
            @Valid @RequestBody NoticeRequest request) {
        noticeService.updateNotice(userId, noticeId, request);
    }

    @Operation(summary = "공지 삭제")
    @DeleteMapping("/{noticeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotice(
            @LoginUser Long userId,
            @Parameter(description = "공지 ID") @PathVariable Long noticeId) {
        noticeService.deleteNotice(userId, noticeId);
    }
}
