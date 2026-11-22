package com.certsniper.admin.controller;

import com.certsniper.admin.dto.InquiryAnswerRequest;
import com.certsniper.global.common.PageResponse;
import com.certsniper.global.security.LoginUser;
import com.certsniper.support.dto.InquiryResponse;
import com.certsniper.support.service.InquiryService;
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

@Tag(name = "16. 관리자")
@RestController
@RequestMapping("/api/v1/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "문의 목록")
    @GetMapping
    public PageResponse<InquiryResponse> getInquiriesForAdmin(
            @Parameter(description = "상태") @RequestParam(name = "status", required = false) String status,
            @Parameter(description = "대상 회원 ID") @RequestParam(name = "user_id", required = false) Long targetUserId,
            @Parameter(description = "페이지 (0부터)") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "10") int size) {
        return inquiryService.getInquiriesForAdmin(status, targetUserId, page, size);
    }

    @Operation(summary = "문의 답변 등록")
    @PostMapping("/{inquiryId}/answer")
    @ResponseStatus(HttpStatus.CREATED)
    public void answerInquiry(
            @LoginUser Long userId,
            @Parameter(description = "문의 ID") @PathVariable Long inquiryId,
            @Valid @RequestBody InquiryAnswerRequest request) {
        inquiryService.answerInquiry(userId, inquiryId, request);
    }
}
