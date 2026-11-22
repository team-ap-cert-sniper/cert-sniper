package com.certsniper.rank.controller;

import com.certsniper.global.security.LoginUser;
import com.certsniper.rank.dto.RankResponse;
import com.certsniper.rank.service.RankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "14. 저격 등급")
@RestController
@RequestMapping("/api/v1/ranks")
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    @Operation(summary = "내 저격 등급")
    @GetMapping("/me")
    public RankResponse getMyRank(@LoginUser Long userId) {
        return rankService.getMyRank(userId);
    }
}
