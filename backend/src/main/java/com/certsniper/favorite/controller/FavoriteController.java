package com.certsniper.favorite.controller;

import com.certsniper.favorite.dto.FavoriteResponse;
import com.certsniper.favorite.service.FavoriteService;
import com.certsniper.global.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "07. 관심 자격증")
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "관심 자격증 목록")
    @GetMapping
    public List<FavoriteResponse> getFavorites(@LoginUser Long userId) {
        return favoriteService.getFavorites(userId);
    }

    @Operation(summary = "찜 등록")
    @PostMapping("/{jmcd}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addFavorite(
            @LoginUser Long userId,
            @Parameter(description = "자격 종목코드") @PathVariable String jmcd) {
        favoriteService.addFavorite(userId, jmcd);
    }

    @Operation(summary = "찜 해제")
    @DeleteMapping("/{jmcd}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(
            @LoginUser Long userId,
            @Parameter(description = "자격 종목코드") @PathVariable String jmcd) {
        favoriteService.removeFavorite(userId, jmcd);
    }
}
