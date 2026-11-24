package com.certsniper.support.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.certsniper.admin.dto.NoticeRequest;
import com.certsniper.admin.service.AuditLogService;
import com.certsniper.global.common.PageResponse;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.support.dto.NoticeResponse;
import com.certsniper.support.entity.Notice;
import com.certsniper.support.repository.NoticeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private AuditLogService auditLogService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private NoticeService noticeService;

    @Test
    void 공지_목록은_본문_없이_반환한다() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(noticeRepository.findByIsDeletedFalseOrderByCreatedAtDescIdDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(row(1L, "GENERAL", "첫 공지")), pageable, 1));

        PageResponse<NoticeResponse> result = noticeService.getNotices(0, 5);

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(5);
        assertThat(result.items()).singleElement().satisfies(item -> {
            assertThat(item.title()).isEqualTo("첫 공지");
            assertThat(item.content()).isNull();
        });
    }

    @Test
    void 페이지_값이_잘못되면_입력_오류() {
        assertErrorCode(() -> noticeService.getNotices(-1, 5), ErrorCode.VALIDATION_ERROR);
        assertErrorCode(() -> noticeService.getNotices(0, 0), ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void 없거나_삭제된_공지_상세는_404() {
        when(noticeRepository.findByIdAndIsDeletedFalse(9L)).thenReturn(Optional.empty());

        assertErrorCode(() -> noticeService.getNotice(9L), ErrorCode.NOT_FOUND);
    }

    @Test
    void 공지_상세는_본문을_포함한다() {
        when(noticeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(notice(1L, "본문")));

        assertThat(noticeService.getNotice(1L).content()).isEqualTo("본문");
    }

    @Test
    void 공지_등록은_본문을_필터링하고_이력을_남긴다() {
        when(noticeRepository.save(any(Notice.class))).thenAnswer(invocation -> {
            Notice saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 3L);
            return saved;
        });

        noticeService.createNotice(1L, new NoticeRequest("MAINTENANCE", "점검 안내", "<p>점검</p><script>alert(1)</script>"));

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
        verify(noticeRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("<p>점검</p>");
        assertThat(captor.getValue().getAuthorId()).isEqualTo(1L);
        verify(auditLogService).record(eq(1L), isNull(), eq("CREATE"), eq("notices"), eq("3"), isNull(), anyString(), isNull());
    }

    @Test
    void 공지_유형이_잘못되면_저장하지_않는다() {
        assertErrorCode(() -> noticeService.createNotice(1L, new NoticeRequest("EVENT", "제목", "본문")),
                ErrorCode.VALIDATION_ERROR);
        verify(noticeRepository, never()).save(any());
    }

    @Test
    void 필터링_후_글자가_없으면_입력_오류() {
        assertErrorCode(() -> noticeService.createNotice(1L, new NoticeRequest("GENERAL", "제목", "<script>x</script>")),
                ErrorCode.VALIDATION_ERROR);
        verify(noticeRepository, never()).save(any());
    }

    @Test
    void 공지_수정은_변경_전후를_기록한다() {
        Notice notice = notice(1L, "이전 본문");
        when(noticeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(notice));

        noticeService.updateNotice(2L, 1L, new NoticeRequest("GENERAL", "새 제목", "새 본문"));

        assertThat(notice.getTitle()).isEqualTo("새 제목");
        assertThat(notice.getAuthorId()).isEqualTo(1L);
        ArgumentCaptor<String> before = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> after = ArgumentCaptor.forClass(String.class);
        verify(auditLogService).record(eq(2L), isNull(), eq("UPDATE"), eq("notices"), eq("1"),
                before.capture(), after.capture(), isNull());
        assertThat(before.getValue()).contains("이전 본문");
        assertThat(after.getValue()).contains("새 본문");
    }

    @Test
    void 공지_삭제는_상태값만_바꾼다() {
        Notice notice = notice(1L, "본문");
        when(noticeRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(notice));

        noticeService.deleteNotice(2L, 1L);

        assertThat(notice.getIsDeleted()).isTrue();
        verify(noticeRepository, never()).delete(any());
        verify(auditLogService).record(eq(2L), isNull(), eq("DELETE"), eq("notices"), eq("1"), anyString(), isNull(), isNull());
    }

    @Test
    void 공지_수는_삭제되지_않은_공지만_센다() {
        when(noticeRepository.countByIsDeletedFalse()).thenReturn(4L);

        assertThat(noticeService.countNotices()).isEqualTo(4L);
    }

    private static Notice notice(Long id, String content) {
        Notice notice = new Notice(1L, "GENERAL", "제목", content);
        ReflectionTestUtils.setField(notice, "id", id);
        return notice;
    }

    private static NoticeRepository.ListRow row(Long id, String noticeType, String title) {
        return new NoticeRepository.ListRow() {
            public Long getId() {
                return id;
            }

            public String getNoticeType() {
                return noticeType;
            }

            public String getTitle() {
                return title;
            }

            public LocalDateTime getCreatedAt() {
                return LocalDateTime.of(2026, 11, 24, 10, 0);
            }
        };
    }

    private static void assertErrorCode(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
