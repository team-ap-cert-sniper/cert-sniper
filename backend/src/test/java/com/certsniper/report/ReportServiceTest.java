package com.certsniper.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.passstory.dto.PassStoryRequest;
import com.certsniper.passstory.entity.PassStory;
import com.certsniper.passstory.repository.PassStoryRepository;
import com.certsniper.passstory.service.PassStoryService;
import com.certsniper.qualification.service.QualificationService;
import com.certsniper.report.dto.ReportRequest;
import com.certsniper.report.repository.PassStoryModerationLogRepository;
import com.certsniper.report.repository.PassStoryReportRepository;
import com.certsniper.report.service.ReportService;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReportServiceTest {

    private static final Long AUTHOR = 1L;
    private static final Long REPORTER = 2L;
    private static final Long STORY = 10L;

    private final PassStoryReportRepository reportRepo = mock(PassStoryReportRepository.class);
    private final PassStoryModerationLogRepository logRepo = mock(PassStoryModerationLogRepository.class);
    private final PassStoryRepository storyRepo = mock(PassStoryRepository.class);
    private final PassStoryService passStoryService = mock(PassStoryService.class);
    private final ReportService service =
            new ReportService(reportRepo, logRepo, storyRepo, passStoryService, mock(QualificationService.class));
    private final ReportRequest abuse = new ReportRequest("ABUSE");

    private void stored(String status) {
        PassStory s = PassStory.create(AUTHOR, new PassStoryRequest("1320", "b", null, null, null, null, null, null), "b");
        s.changeStatus(status);
        when(storyRepo.findById(STORY)).thenReturn(Optional.of(s));
        when(reportRepo.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void 본인_글_신고는_FORBIDDEN() {
        stored("PUBLISHED");
        assertEquals(ErrorCode.FORBIDDEN,
                assertThrows(BusinessException.class, () -> service.reportPassStory(AUTHOR, STORY, abuse)).getErrorCode());
    }

    @Test
    void 같은_글_재신고는_DUPLICATE() {
        stored("PUBLISHED");
        when(reportRepo.existsByStoryIdAndReporterId(STORY, REPORTER)).thenReturn(true);
        assertEquals(ErrorCode.DUPLICATE,
                assertThrows(BusinessException.class, () -> service.reportPassStory(REPORTER, STORY, abuse)).getErrorCode());
    }

    @Test
    void 공개_글_신고는_즉시_숨김과_이력() {
        stored("PUBLISHED");
        service.reportPassStory(REPORTER, STORY, abuse);
        verify(passStoryService).hideByReport(STORY, "ABUSE");
        verify(logRepo).save(any());
    }

    @Test
    void 이미_숨김인_글_신고는_이력_없음() {
        stored("HIDDEN");
        service.reportPassStory(REPORTER, STORY, abuse);
        verify(reportRepo).save(any());
        verify(logRepo, never()).save(any());
    }
}
