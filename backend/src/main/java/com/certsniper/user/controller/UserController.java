package com.certsniper.user.controller;

import com.certsniper.global.security.LoginUser;
import com.certsniper.user.dto.UserProfileResponse;
import com.certsniper.user.dto.UserUpdateRequest;
import com.certsniper.user.dto.WithdrawRequest;
import com.certsniper.user.service.UserService;
import com.certsniper.user.service.WithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "02. 회원정보")
@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final WithdrawalService withdrawalService;

    @Operation(summary = "내 회원정보 조회")
    @GetMapping
    public UserProfileResponse getMyProfile(@LoginUser Long userId) {
        return userService.getMyProfile(userId);
    }

    @Operation(summary = "내 회원정보 수정")
    @PatchMapping
    public UserProfileResponse updateMyProfile(
            @LoginUser Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        return userService.updateMyProfile(userId, request);
    }

    @Operation(summary = "회원 탈퇴 신청 (7일 유예)")
    @PostMapping("/withdrawal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestWithdrawal(
            @LoginUser Long userId,
            @Valid @RequestBody WithdrawRequest request) {
        withdrawalService.requestWithdrawal(userId, request);
    }

    @Operation(summary = "회원 탈퇴 철회")
    @DeleteMapping("/withdrawal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelWithdrawal(@LoginUser Long userId) {
        withdrawalService.cancelWithdrawal(userId);
    }
}
