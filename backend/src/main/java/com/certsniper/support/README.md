# 📁 support (고객센터) [황정규]

공지사항·1:1 문의 백엔드입니다. FAQ는 프론트에 고정 문구로 넣으므로 이 패키지에서 구현하지 않습니다.

* 기능 ID: CS-02 공지 · CS-04 문의 등록 · CS-05 나의 문의 / 관리자 쪽 ADMIN-03 공지 관리 · ADMIN-04 문의 답변 · ADMIN-10 건수
* 테이블: `notices`, `inquiries`, `inquiry_answers` (`faqs`는 사용 안 함)
* 화면: [frontend/src/pages/Support](../../../../../../../frontend/src/pages/Support/README.md), 관리자 화면은 [frontend/src/pages/Admin](../../../../../../../frontend/src/pages/Admin/README.md)

## API

| 메서드 | 경로 | 로그인 | 서비스 메서드 | 응답 |
|---|---|---|---|---|
| GET | `/api/v1/notices?page&size` | 회원 | `NoticeService.getNotices(page, size)` | 200 `PageResponse<NoticeResponse>` |
| GET | `/api/v1/notices/{noticeId}` | 회원 | `NoticeService.getNotice(noticeId)` | 200 `NoticeResponse` |
| POST | `/api/v1/inquiries` | 회원 | `InquiryService.createInquiry(userId, request)` | 201 (본문 없음) |
| GET | `/api/v1/inquiries?page&size` | 회원 | `InquiryService.getMyInquiries(userId, page, size)` | 200 `PageResponse<InquiryResponse>` |
| GET | `/api/v1/inquiries/{inquiryId}` | 회원 | `InquiryService.getMyInquiry(userId, inquiryId)` | 200 `InquiryResponse` |
| GET | `/api/v1/faqs` | 회원 | `FaqService.getFaqs()` | **구현 안 함** (501 유지) |

관리자 API(`admin/controller`)가 이 패키지 서비스를 부릅니다.

| 메서드 | 경로 | 서비스 메서드 | 응답 |
|---|---|---|---|
| POST | `/api/v1/admin/notices` | `NoticeService.createNotice(userId, request)` | 201 |
| PUT | `/api/v1/admin/notices/{noticeId}` | `NoticeService.updateNotice(userId, noticeId, request)` | 204 |
| DELETE | `/api/v1/admin/notices/{noticeId}` | `NoticeService.deleteNotice(userId, noticeId)` | 204 |
| GET | `/api/v1/admin/inquiries?status&user_id&page&size` | `InquiryService.getInquiriesForAdmin(status, targetUserId, page, size)` | 200 `PageResponse<InquiryResponse>` |
| POST | `/api/v1/admin/inquiries/{inquiryId}/answer` | `InquiryService.answerInquiry(userId, inquiryId, request)` | 201 |

## 구현 규칙

| 항목 | 규칙 | 근거 |
|---|---|---|
| 목록 크기 | 컨트롤러 기본값은 10이지만 프론트가 `size=5`로 부름 | 개발 시작 공지 |
| 목록 컬럼 | 공지·문의 목록은 `content`·`answer`를 `null`로 반환, 상세에서만 채움 | 설계서 공통 기준 9 |
| 공지 정렬 | `is_deleted = 0 ORDER BY created_at DESC` (IDX `is_deleted, created_at`) | 설계서 notices |
| 공지 삭제 | `is_deleted = 1`로 변경 (행 삭제 안 함). 삭제된 공지 상세는 404 `NOT_FOUND` | 설계서 notices |
| 공지 본문 | 저장 전 `HtmlSanitizer.sanitize` (global, 수정 안 하고 사용만) | 설계서 notices |
| 공지 유형 | `GENERAL` · `MAINTENANCE` 외 값은 400 `VALIDATION_ERROR` | DB CHECK |
| 공지 작성자 | 화면에 표시 안 함 (`author_id`는 저장만) | 2026-11-24 결정 |
| 나의 문의 목록 | `user_id = ? ORDER BY created_at DESC` (IDX `user_id, created_at`) | 설계서 inquiries |
| 다른 회원 문의 | 상세 조회 시 403 `FORBIDDEN` (확인 필요 Q11) | 기능명세서 CS-05 |
| 답변 | 문의당 1건. 이미 답변이 있으면 `DUPLICATE` 오류. 저장과 함께 `status = ANSWERED` | UQ(`inquiry_id`) |
| 닉네임 | 관리자 문의 목록만 `UserService.getNicknames(userIds)`로 한 번에 (N+1 금지). 나의 문의 목록·상세는 본인 글이라 `nickname = null` | 개발 시작 공지 |
| 목록 컬럼 조회 | 리포지토리 안 `ListRow` 인터페이스 프로젝션으로 본문 컬럼을 SELECT하지 않음 | 설계서 공통 기준 9 |
| 페이지 값 | `page < 0`, `size < 1`, `size > 100`이면 `VALIDATION_ERROR` | |
| 관리자 기록 | 공지 등록·수정·삭제, 답변 저장 시 `AuditLogService.record` 호출 (CREATE · UPDATE · DELETE · ANSWER) | 설계서 admin_audit_logs |
| 날짜 | `DateUtils.now()` | backend README |

## 다른 패키지와 주고받는 메서드

| 방향 | 메서드 | 쓰는 곳 |
|---|---|---|
| 제공 | `NoticeService.countNotices()`, `InquiryService.countUnanswered()` | admin 대시보드 (ADMIN-10) |
| 제공 | `NoticeService.createNotice` · `updateNotice` · `deleteNotice` | admin 공지 관리 (ADMIN-03) |
| 제공 | `InquiryService.getInquiriesForAdmin` · `answerInquiry` | admin 문의 답변 (ADMIN-04) |
| 사용 | `UserService.getNicknames` (최기완) | 문의 목록 닉네임 |
| 사용 | `AuditLogService.record` (admin, 황정규) | 관리자 변경 이력 |
| 사용 | `HtmlSanitizer`, `DateUtils`, `PageResponse`, `BusinessException` (global) | 공통 |

## 이 패키지에서 고치는 파일

| 파일 | 할 일 | Phase |
|---|---|---|
| `entity/Notice.java` | 생성자, `update(...)`, `delete()` 추가 | 2 |
| `entity/Inquiry.java` | 생성자, `markAnswered()` 추가 | 2 |
| `entity/InquiryAnswer.java` | 생성자 추가 | 2 |
| `repository/NoticeRepository.java` | 삭제 안 된 공지 페이지 조회, 건수 | 2 |
| `repository/InquiryRepository.java` | 회원별·상태별 페이지 조회, 미답변 건수 | 2 |
| `repository/InquiryAnswerRepository.java` | 문의 ID로 답변 조회·존재 확인 | 2 |
| `service/NoticeService.java` | 메서드 6개 구현 | 2 |
| `service/InquiryService.java` | 메서드 6개 구현 | 2 |
| `service/FaqService.java` | 고치지 않음 | — |

테스트: `backend/src/test/java/com/certsniper/support/service/NoticeServiceTest.java`, `InquiryServiceTest.java` (Phase 2)

## 확인이 필요한 것

| 번호 | 내용 | 상태 |
|---|---|---|
| Q3 | 관리자 문의 검색에 유형 필터 없음, 관리자용 문의 상세 API 없음 | 팀 확인 중 |
| Q11 | 다른 회원 문의 상세 조회 시 403으로 할지 404로 할지 | 팀 확인 중 |
| S4 | 기능명세서 CS-04는 접수 후 문의 ID를 준다고 되어 있지만 API는 본문 없음 | 명세서 수정 요청 |
| — | `faqs` 테이블·`/api/v1/faqs`는 쓰지 않음. 지울지는 팀 결정 | 공유만 |
