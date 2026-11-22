package com.certsniper.support.controller;

import com.certsniper.global.common.PageResponse;
import com.certsniper.global.security.LoginUser;
import com.certsniper.support.dto.InquiryRequest;
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

@Tag(name = "15. 고객센터")
@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "1:1 문의 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createInquiry(
            @LoginUser Long userId,
            @Valid @RequestBody InquiryRequest request) {
        inquiryService.createInquiry(userId, request);
    }

    @Operation(summary = "나의 문의 내역")
    @GetMapping
    public PageResponse<InquiryResponse> getMyInquiries(
            @LoginUser Long userId,
            @Parameter(description = "페이지 (0부터)") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "10") int size) {
        return inquiryService.getMyInquiries(userId, page, size);
    }

    @Operation(summary = "나의 문의 상세")
    @GetMapping("/{inquiryId}")
    public InquiryResponse getMyInquiry(
            @LoginUser Long userId,
            @Parameter(description = "문의 ID") @PathVariable Long inquiryId) {
        return inquiryService.getMyInquiry(userId, inquiryId);
    }
}
