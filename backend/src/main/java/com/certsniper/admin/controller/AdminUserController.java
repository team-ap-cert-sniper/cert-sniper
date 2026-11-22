package com.certsniper.admin.controller;

import com.certsniper.admin.dto.AdminUserDetailResponse;
import com.certsniper.admin.dto.ProfileCorrectionRequest;
import com.certsniper.admin.dto.SuspendRequest;
import com.certsniper.admin.service.AdminUserService;
import com.certsniper.global.common.PageResponse;
import com.certsniper.global.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "16. 관리자")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "회원 목록")
    @GetMapping
    public PageResponse<AdminUserDetailResponse> getUsers(
            @Parameter(description = "검색어") @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "상태") @RequestParam(name = "status", required = false) String status,
            @Parameter(description = "페이지 (0부터)") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "10") int size) {
        return adminUserService.getUsers(keyword, status, page, size);
    }

    @Operation(summary = "회원 상세")
    @GetMapping("/{targetUserId}")
    public AdminUserDetailResponse getUser(@Parameter(description = "대상 회원 ID") @PathVariable Long targetUserId) {
        return adminUserService.getUser(targetUserId);
    }

    @Operation(summary = "회원 정보 정정")
    @PatchMapping("/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void correctUser(
            @LoginUser Long userId,
            @Parameter(description = "대상 회원 ID") @PathVariable Long targetUserId,
            @Valid @RequestBody ProfileCorrectionRequest request) {
        adminUserService.correctUser(userId, targetUserId, request);
    }

    @Operation(summary = "계정 정지")
    @PostMapping("/{targetUserId}/suspension")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void suspendUser(
            @LoginUser Long userId,
            @Parameter(description = "대상 회원 ID") @PathVariable Long targetUserId,
            @Valid @RequestBody SuspendRequest request) {
        adminUserService.suspendUser(userId, targetUserId, request);
    }

    @Operation(summary = "계정 정지 해제")
    @DeleteMapping("/{targetUserId}/suspension")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsuspendUser(
            @LoginUser Long userId,
            @Parameter(description = "대상 회원 ID") @PathVariable Long targetUserId) {
        adminUserService.unsuspendUser(userId, targetUserId);
    }
}
