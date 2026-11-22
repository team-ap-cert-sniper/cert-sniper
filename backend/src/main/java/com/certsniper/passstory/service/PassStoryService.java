package com.certsniper.passstory.service;

import com.certsniper.global.common.PageResponse;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.passstory.dto.PassStoryCardResponse;
import com.certsniper.passstory.dto.PassStoryDetailResponse;
import com.certsniper.passstory.dto.PassStoryRequest;
import com.certsniper.passstory.repository.PassStoryRepository;
import com.certsniper.rank.dto.RankUpResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PassStoryService {

    private final PassStoryRepository passStoryRepository;

    public PageResponse<PassStoryCardResponse> getPassStories(
            Long userId,
            String keyword,
            List<String> jmcds,
            boolean mine,
            String sort,
            int page,
            int size) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    public PassStoryDetailResponse getPassStory(Long userId, Long storyId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public RankUpResponse createPassStory(Long userId, PassStoryRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void updatePassStory(Long userId, Long storyId, PassStoryRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void deletePassStory(Long userId, Long storyId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    public PageResponse<PassStoryCardResponse> getPassStoriesForAdmin(
            String nickname,
            String status,
            int page,
            int size) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** report 신고: 즉시 숨김 */
    @Transactional
    public void hideByReport(Long storyId, String reportType) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** admin 신고 검토·재검토: 게시 상태 변경 */
    @Transactional
    public void changeStatus(Long storyId, String newStatus, String hiddenReason) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
