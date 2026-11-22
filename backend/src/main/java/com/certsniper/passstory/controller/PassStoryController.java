package com.certsniper.passstory.controller;

import com.certsniper.global.common.PageResponse;
import com.certsniper.global.security.LoginUser;
import com.certsniper.passstory.dto.PassStoryCardResponse;
import com.certsniper.passstory.dto.PassStoryDetailResponse;
import com.certsniper.passstory.dto.PassStoryRequest;
import com.certsniper.passstory.service.PassStoryService;
import com.certsniper.rank.dto.RankUpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "12. 저격왕 합격수기")
@RestController
@RequestMapping("/api/v1/pass-stories")
@RequiredArgsConstructor
public class PassStoryController {

    private final PassStoryService passStoryService;

    @Operation(summary = "합격수기 목록·검색 (jmcds: 추천 페이지 미리보기)")
    @GetMapping
    public PageResponse<PassStoryCardResponse> getPassStories(
            @LoginUser Long userId,
            @Parameter(description = "검색어") @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "자격 종목코드 목록 (추천 페이지 미리보기)") @RequestParam(name = "jmcds", required = false) List<String> jmcds,
            @Parameter(description = "내가 쓴 글만") @RequestParam(name = "mine", defaultValue = "false") boolean mine,
            @Parameter(description = "정렬 (LATEST 등)") @RequestParam(name = "sort", defaultValue = "LATEST") String sort,
            @Parameter(description = "페이지 (0부터)") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "10") int size) {
        return passStoryService.getPassStories(userId, keyword, jmcds, mine, sort, page, size);
    }

    @Operation(summary = "합격수기 상세")
    @GetMapping("/{storyId}")
    public PassStoryDetailResponse getPassStory(
            @LoginUser Long userId,
            @Parameter(description = "게시글 ID") @PathVariable Long storyId) {
        return passStoryService.getPassStory(userId, storyId);
    }

    @Operation(summary = "합격수기 등록 (승급이면 이전·새 등급)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RankUpResponse createPassStory(
            @LoginUser Long userId,
            @Valid @RequestBody PassStoryRequest request) {
        return passStoryService.createPassStory(userId, request);
    }

    @Operation(summary = "합격수기 수정")
    @PutMapping("/{storyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePassStory(
            @LoginUser Long userId,
            @Parameter(description = "게시글 ID") @PathVariable Long storyId,
            @Valid @RequestBody PassStoryRequest request) {
        passStoryService.updatePassStory(userId, storyId, request);
    }

    @Operation(summary = "합격수기 삭제")
    @DeleteMapping("/{storyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePassStory(
            @LoginUser Long userId,
            @Parameter(description = "게시글 ID") @PathVariable Long storyId) {
        passStoryService.deletePassStory(userId, storyId);
    }
}
