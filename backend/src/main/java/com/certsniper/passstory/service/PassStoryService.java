package com.certsniper.passstory.service;

import com.certsniper.global.common.PageResponse;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.DateUtils;
import com.certsniper.global.util.HtmlSanitizer;
import com.certsniper.passstory.dto.PassStoryCardResponse;
import com.certsniper.passstory.dto.PassStoryDetailResponse;
import com.certsniper.passstory.dto.PassStoryRequest;
import com.certsniper.passstory.entity.PassStory;
import com.certsniper.passstory.repository.PassStoryRepository;
import com.certsniper.qualification.service.QualificationService;
import com.certsniper.rank.dto.RankUpResponse;
import com.certsniper.rank.service.RankService;
import com.certsniper.report.service.ReportService;
import com.certsniper.user.service.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PassStoryService {

    private static final int PREVIEW_LENGTH = 100;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_BODY_LENGTH = 1500;
    private static final Set<String> STATUSES = Set.of("PUBLISHED", "HIDDEN", "REVIEW_PENDING", "DELETED");
    private static final Set<String> REASONS = Set.of("IRRELEVANT", "ABUSE", "PRIVACY", "AD", "ETC");

    private final PassStoryRepository passStoryRepository;
    private final RankService rankService;
    private final ReportService reportService;
    private final UserService userService;
    private final QualificationService qualificationService;

    /** 전체: 공개 글만 / jmcds: 해당 종목 공개 글 / mine: 삭제 제외 내 글 전부(숨김·재검토 대기 포함) */
    public PageResponse<PassStoryCardResponse> getPassStories(
            Long userId,
            String keyword,
            List<String> jmcds,
            boolean mine,
            String sort,
            int page,
            int size) {
        if (!"LATEST".equals(sort) || page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (keyword != null && !keyword.isBlank()) {
            // 자격증 이름 → jmcd 변환 수단이 팀에서 정해지지 않음 (kiwan.md Q1)
            throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        Page<PassStory> stories;
        if (mine) {
            stories = passStoryRepository.findByUserIdAndStatusNot(userId, "DELETED", pageable);
        } else if (jmcds != null && !jmcds.isEmpty()) {
            stories = passStoryRepository.findByJmcdInAndStatus(jmcds, "PUBLISHED", pageable);
        } else {
            stories = passStoryRepository.findByStatus("PUBLISHED", pageable);
        }
        return PageResponse.of(toCards(stories));
    }

    private Page<PassStoryCardResponse> toCards(Page<PassStory> stories) {
        Set<Long> userIds = stories.stream().map(PassStory::getUserId).collect(Collectors.toSet());
        Map<Long, String> nicknames = userService.getNicknames(userIds);
        Map<Long, String> ranks = rankService.getCurrentRanks(userIds);
        Map<String, String> names = new HashMap<>();
        return stories.map(s -> new PassStoryCardResponse(
                s.getId(),
                s.getJmcd(),
                names.computeIfAbsent(s.getJmcd(), this::qualificationName),
                nicknames.get(s.getUserId()),
                ranks.get(s.getUserId()),
                preview(s.getBody()),
                s.getPrepPeriod(),
                s.getAttemptBand(),
                s.getStatus(),
                s.getCreatedAt()));
    }

    private static String preview(String body) {
        String text = HtmlSanitizer.toText(body);
        return text.length() <= PREVIEW_LENGTH ? text : text.substring(0, PREVIEW_LENGTH);
    }

    /** 없는 종목이면 getInfo가 NOT_FOUND */
    private String qualificationName(String jmcd) {
        return qualificationService.getInfo(jmcd).jmfldnm();
    }

    /** 타인의 숨김·재검토 대기 글은 본문·교재명을 빼고 반환 (화면은 status로 숨김 안내) */
    public PassStoryDetailResponse getPassStory(Long userId, Long storyId) {
        PassStory s = findActive(storyId);
        boolean mine = s.getUserId().equals(userId);
        boolean visible = mine || "PUBLISHED".equals(s.getStatus());
        return new PassStoryDetailResponse(
                s.getId(),
                s.getUserId(),
                userService.getNicknames(List.of(s.getUserId())).get(s.getUserId()),
                rankService.getCurrentRank(s.getUserId()),
                s.getJmcd(),
                qualificationName(s.getJmcd()),
                visible ? s.getBody() : null,
                s.getExamineeBase(),
                s.getPrepPeriod(),
                s.getAttemptBand(),
                visible ? s.getTextbookName() : null,
                s.getPassScore(),
                s.getExamDate(),
                s.getStatus(),
                s.getHiddenReason(),
                mine,
                s.getCreatedAt());
    }

    @Transactional
    public RankUpResponse createPassStory(Long userId, PassStoryRequest request) {
        qualificationName(request.jmcd());
        passStoryRepository.save(PassStory.create(userId, request, sanitizeBody(request.body())));
        return rankService.recalculate(userId);
    }

    /** 공개 글은 그대로 공개, 숨김·재검토 대기 글은 재검토 대기로. 종목이 바뀔 수 있어 등급도 다시 계산 */
    @Transactional
    public void updatePassStory(Long userId, Long storyId, PassStoryRequest request) {
        PassStory s = findOwnedActive(userId, storyId);
        qualificationName(request.jmcd());
        String previous = s.getStatus();
        boolean published = "PUBLISHED".equals(previous);
        String next = published ? "PUBLISHED" : "REVIEW_PENDING";
        s.edit(request, sanitizeBody(request.body()));
        s.changeStatus(next);
        reportService.recordModeration(storyId, published ? "AUTHOR_EDIT" : "RESUBMIT", userId, "AUTHOR",
                null, null, previous, next);
        rankService.recalculate(userId);
    }

    @Transactional
    public void deletePassStory(Long userId, Long storyId) {
        PassStory s = findOwnedActive(userId, storyId);
        String previous = s.getStatus();
        s.changeStatus("DELETED");
        reportService.recordModeration(storyId, "AUTHOR_DELETE", userId, "AUTHOR", null, null, previous, "DELETED");
        rankService.recalculate(userId);
    }

    public PageResponse<PassStoryCardResponse> getPassStoriesForAdmin(
            String nickname,
            String status,
            int page,
            int size) {
        // Phase 3: 닉네임 검색은 UserService.searchUsers 구현 후
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** report 신고: 즉시 숨김. 이미 숨김·재검토 대기면 상태 유지 */
    @Transactional
    public void hideByReport(Long storyId, String reportType) {
        PassStory s = findActive(storyId);
        if ("PUBLISHED".equals(s.getStatus())) {
            s.hide(reportType);
        }
    }

    /**
     * admin 신고 검토·재검토: 게시 상태 변경.
     * HIDDEN = 숨김 확정(확정 시각 기록), PUBLISHED = 숨김 해제(사유·확정 시각 삭제).
     * 이력 기록·등급 재계산은 admin이 recordModeration·recalculate로 직접 호출
     */
    @Transactional
    public void changeStatus(Long storyId, String newStatus, String hiddenReason) {
        if (!STATUSES.contains(newStatus) || (hiddenReason != null && !REASONS.contains(hiddenReason))) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        PassStory s = passStoryRepository.findById(storyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        switch (newStatus) {
            case "HIDDEN" -> {
                s.hide(hiddenReason != null ? hiddenReason : s.getHiddenReason());
                s.confirmHidden(DateUtils.now());
            }
            case "PUBLISHED" -> {
                s.changeStatus(newStatus);
                s.clearHidden();
            }
            default -> s.changeStatus(newStatus);
        }
    }

    private PassStory findActive(Long storyId) {
        return passStoryRepository.findById(storyId)
                .filter(s -> !"DELETED".equals(s.getStatus()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private PassStory findOwnedActive(Long userId, Long storyId) {
        PassStory s = findActive(storyId);
        if (!s.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return s;
    }

    /** 허용 태그만 남긴 뒤 길이 재확인 (이스케이프로 늘어나면 DB CHECK 1~1500 위반) */
    private static String sanitizeBody(String body) {
        String clean = HtmlSanitizer.sanitize(body);
        if (HtmlSanitizer.toText(clean).isBlank() || clean.length() > MAX_BODY_LENGTH) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return clean;
    }
}
