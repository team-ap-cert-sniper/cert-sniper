package com.certsniper.favorite.service;

import com.certsniper.favorite.dto.FavoriteResponse;
import com.certsniper.favorite.repository.FavoriteRepository;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public List<FavoriteResponse> getFavorites(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void addFavorite(Long userId, String jmcd) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void removeFavorite(Long userId, String jmcd) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** recommendation 카드: 찜한 종목코드 */
    public Set<String> getFavoriteJmcds(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** user 탈퇴 확정: 찜 전체 삭제 */
    @Transactional
    public void deleteAllByUser(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
