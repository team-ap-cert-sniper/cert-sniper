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

import com.certsniper.admin.dto.InquiryAnswerRequest;
import com.certsniper.admin.service.AuditLogService;
import com.certsniper.global.common.PageResponse;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.support.dto.InquiryRequest;
import com.certsniper.support.dto.InquiryResponse;
import com.certsniper.support.entity.Inquiry;
import com.certsniper.support.entity.InquiryAnswer;
import com.certsniper.support.enums.InquiryType;
import com.certsniper.support.repository.InquiryAnswerRepository;
import com.certsniper.support.repository.InquiryRepository;
import com.certsniper.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private InquiryAnswerRepository inquiryAnswerRepository;

    @Mock
    private UserService userService;

    @Mock
    private AuditLogService auditLogService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private InquiryService inquiryService;

    @Test
    void 문의_등록은_대기_상태로_저장한다() {
        inquiryService.createInquiry(1L, new InquiryRequest(InquiryType.PLANNER, "제목", "내용"));

        ArgumentCaptor<Inquiry> captor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getInquiryType()).isEqualTo("PLANNER");
        assertThat(captor.getValue().getStatus()).isEqualTo("WAITING");
    }

    @Test
    void 나의_문의_목록은_내용과_답변_없이_반환한다() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(inquiryRepository.findByUserIdOrderByCreatedAtDescIdDesc(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(row(10L, 1L, "WAITING")), pageable, 1));

        PageResponse<InquiryResponse> result = inquiryService.getMyInquiries(1L, 0, 5);

        assertThat(result.items()).singleElement().satisfies(item -> {
            assertThat(item.inquiryType()).isEqualTo(InquiryType.ACCOUNT);
            assertThat(item.content()).isNull();
            assertThat(item.answer()).isNull();
        });
        verify(userService, never()).getNicknames(any());
    }

    @Test
    void 다른_회원의_문의_상세는_403() {
        when(inquiryRepository.findById(10L)).thenReturn(Optional.of(inquiry(10L, 2L)));

        assertErrorCode(() -> inquiryService.getMyInquiry(1L, 10L), ErrorCode.FORBIDDEN);
    }

    @Test
    void 없는_문의_상세는_404() {
        when(inquiryRepository.findById(10L)).thenReturn(Optional.empty());

        assertErrorCode(() -> inquiryService.getMyInquiry(1L, 10L), ErrorCode.NOT_FOUND);
    }

    @Test
    void 나의_문의_상세는_내용과_답변을_포함한다() {
        when(inquiryRepository.findById(10L)).thenReturn(Optional.of(inquiry(10L, 1L)));
        when(inquiryAnswerRepository.findByInquiryId(10L)).thenReturn(Optional.of(new InquiryAnswer(10L, 99L, "답변")));

        InquiryResponse result = inquiryService.getMyInquiry(1L, 10L);

        assertThat(result.content()).isEqualTo("내용");
        assertThat(result.answer()).isEqualTo("답변");
    }

    @Test
    void 관리자_목록의_상태값이_잘못되면_입력_오류() {
        assertErrorCode(() -> inquiryService.getInquiriesForAdmin("DONE", null, 0, 5), ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void 관리자_목록은_빈_상태값을_조건_없음으로_보고_닉네임을_한_번에_채운다() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(inquiryRepository.searchForAdmin(null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(row(10L, 1L, "WAITING"), row(11L, 1L, "ANSWERED")), pageable, 2));
        when(userService.getNicknames(Set.of(1L))).thenReturn(Map.of(1L, "저격수"));

        PageResponse<InquiryResponse> result = inquiryService.getInquiriesForAdmin(" ", null, 0, 5);

        assertThat(result.items()).extracting(InquiryResponse::nickname).containsOnly("저격수");
        verify(userService).getNicknames(Set.of(1L));
    }

    @Test
    void 이미_답변한_문의는_중복_오류() {
        Inquiry inquiry = inquiry(10L, 1L);
        inquiry.markAnswered();
        when(inquiryRepository.findById(10L)).thenReturn(Optional.of(inquiry));

        assertErrorCode(() -> inquiryService.answerInquiry(99L, 10L, new InquiryAnswerRequest("답변")), ErrorCode.DUPLICATE);
        verify(inquiryAnswerRepository, never()).saveAndFlush(any());
    }

    @Test
    void 답변_저장은_답변_완료로_바꾸고_이력을_남긴다() {
        Inquiry inquiry = inquiry(10L, 1L);
        when(inquiryRepository.findById(10L)).thenReturn(Optional.of(inquiry));
        when(inquiryAnswerRepository.existsByInquiryId(10L)).thenReturn(false);
        when(inquiryAnswerRepository.saveAndFlush(any(InquiryAnswer.class))).thenAnswer(invocation -> {
            InquiryAnswer saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 7L);
            return saved;
        });

        inquiryService.answerInquiry(99L, 10L, new InquiryAnswerRequest("답변"));

        assertThat(inquiry.getStatus()).isEqualTo("ANSWERED");
        verify(auditLogService).record(eq(99L), eq(1L), eq("ANSWER"), eq("inquiry_answers"), eq("7"),
                isNull(), anyString(), isNull());
    }

    @Test
    void 미답변_문의_수는_대기_상태만_센다() {
        when(inquiryRepository.countByStatus("WAITING")).thenReturn(3L);

        assertThat(inquiryService.countUnanswered()).isEqualTo(3L);
    }

    private static Inquiry inquiry(Long id, Long userId) {
        Inquiry inquiry = new Inquiry(userId, "ACCOUNT", "제목", "내용");
        ReflectionTestUtils.setField(inquiry, "id", id);
        return inquiry;
    }

    private static InquiryRepository.ListRow row(Long id, Long userId, String status) {
        return new InquiryRepository.ListRow() {
            public Long getId() {
                return id;
            }

            public Long getUserId() {
                return userId;
            }

            public String getInquiryType() {
                return "ACCOUNT";
            }

            public String getTitle() {
                return "제목";
            }

            public String getStatus() {
                return status;
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
