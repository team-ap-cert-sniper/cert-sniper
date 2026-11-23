package com.certsniper.passstory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.passstory.dto.PassStoryDetailResponse;
import com.certsniper.passstory.dto.PassStoryRequest;
import com.certsniper.passstory.entity.PassStory;
import com.certsniper.passstory.repository.PassStoryRepository;
import com.certsniper.passstory.service.PassStoryService;
import com.certsniper.qualification.dto.QualificationDetailResponse.QualificationInfo;
import com.certsniper.qualification.service.QualificationService;
import com.certsniper.rank.service.RankService;
import com.certsniper.report.service.ReportService;
import com.certsniper.user.service.UserService;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class PassStoryServiceTest {

    private static final Long AUTHOR = 1L;
    private static final Long OTHER = 2L;
    private static final Long STORY = 10L;

    private final PassStoryRepository repo = mock(PassStoryRepository.class);
    private final RankService rankService = mock(RankService.class);
    private final ReportService reportService = mock(ReportService.class);
    private final UserService userService = mock(UserService.class);
    private final QualificationService qualificationService = mock(QualificationService.class);
    private final PassStoryService service =
            new PassStoryService(repo, rankService, reportService, userService, qualificationService);

    @BeforeEach
    void setUp() {
        QualificationInfo info = mock(QualificationInfo.class);
        when(info.jmfldnm()).thenReturn("정보처리기사");
        when(qualificationService.getInfo(anyString())).thenReturn(info);
        when(userService.getNicknames(any())).thenReturn(Map.of(AUTHOR, "작성자"));
    }

    private static PassStoryRequest request(String body) {
        return new PassStoryRequest("1320", body, null, null, null, "교재", null, null);
    }

    private PassStory stored(String status) {
        PassStory s = PassStory.create(AUTHOR, request("<p>후기</p>"), "<p>후기</p>");
        s.changeStatus(status);
        when(repo.findById(STORY)).thenReturn(Optional.of(s));
        return s;
    }

    private static void assertCode(ErrorCode expected, Executable call) {
        assertEquals(expected, assertThrows(BusinessException.class, call).getErrorCode());
    }

    @Test
    void 숨김_글을_수정하면_재검토_대기() {
        PassStory s = stored("HIDDEN");
        service.updatePassStory(AUTHOR, STORY, request("수정"));
        assertEquals("REVIEW_PENDING", s.getStatus());
        verify(reportService).recordModeration(STORY, "RESUBMIT", AUTHOR, "AUTHOR", null, null, "HIDDEN", "REVIEW_PENDING");
    }

    @Test
    void 공개_글을_수정하면_공개_유지() {
        PassStory s = stored("PUBLISHED");
        service.updatePassStory(AUTHOR, STORY, request("수정"));
        assertEquals("PUBLISHED", s.getStatus());
        verify(reportService).recordModeration(STORY, "AUTHOR_EDIT", AUTHOR, "AUTHOR", null, null, "PUBLISHED", "PUBLISHED");
    }

    @Test
    void 타인_글_수정_삭제는_FORBIDDEN() {
        stored("PUBLISHED");
        assertCode(ErrorCode.FORBIDDEN, () -> service.updatePassStory(OTHER, STORY, request("x")));
        assertCode(ErrorCode.FORBIDDEN, () -> service.deletePassStory(OTHER, STORY));
    }

    @Test
    void 삭제하면_DELETED_이력_등급_재계산() {
        PassStory s = stored("PUBLISHED");
        service.deletePassStory(AUTHOR, STORY);
        assertEquals("DELETED", s.getStatus());
        verify(reportService).recordModeration(STORY, "AUTHOR_DELETE", AUTHOR, "AUTHOR", null, null, "PUBLISHED", "DELETED");
        verify(rankService).recalculate(AUTHOR);
    }

    @Test
    void 삭제된_글_상세는_NOT_FOUND() {
        stored("DELETED");
        assertCode(ErrorCode.NOT_FOUND, () -> service.getPassStory(AUTHOR, STORY));
    }

    @Test
    void 타인의_숨김_글은_본문_없이_반환() {
        stored("HIDDEN");
        PassStoryDetailResponse other = service.getPassStory(OTHER, STORY);
        assertNull(other.body());
        assertNull(other.textbookName());
        PassStoryDetailResponse mine = service.getPassStory(AUTHOR, STORY);
        assertNotNull(mine.body());
        assertEquals(true, mine.mine());
    }

    @Test
    void 이스케이프로_1500자를_넘으면_VALIDATION_ERROR() {
        assertCode(ErrorCode.VALIDATION_ERROR, () -> service.createPassStory(AUTHOR, request("&".repeat(1500))));
    }

    @Test
    void 관리자_HIDDEN은_숨김_확정_PUBLISHED는_해제() {
        PassStory s = stored("HIDDEN");
        service.changeStatus(STORY, "HIDDEN", "ABUSE");
        assertNotNull(s.getHiddenConfirmedAt());
        service.changeStatus(STORY, "PUBLISHED", null);
        assertNull(s.getHiddenConfirmedAt());
        assertNull(s.getHiddenReason());
    }
}
