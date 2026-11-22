package com.certsniper.admin.controller;

import com.certsniper.global.common.PageResponse;
import com.certsniper.passstory.dto.PassStoryCardResponse;
import com.certsniper.passstory.service.PassStoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "16. 관리자")
@RestController
@RequestMapping("/api/v1/admin/pass-stories")
@RequiredArgsConstructor
public class AdminPassStoryController {

    private final PassStoryService passStoryService;

    @Operation(summary = "저격왕 전체 글")
    @GetMapping
    public PageResponse<PassStoryCardResponse> getPassStoriesForAdmin(
            @Parameter(description = "닉네임") @RequestParam(name = "nickname", required = false) String nickname,
            @Parameter(description = "상태") @RequestParam(name = "status", required = false) String status,
            @Parameter(description = "페이지 (0부터)") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "10") int size) {
        return passStoryService.getPassStoriesForAdmin(nickname, status, page, size);
    }
}
