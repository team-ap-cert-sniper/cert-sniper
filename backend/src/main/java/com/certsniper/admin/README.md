# 📁 admin (관리자) [황정규]

관리자 전용 API입니다. 경로는 모두 `/api/v1/admin/**`이고 `SecurityConfig`가 `ADMIN` 권한을 확인합니다.

* 기능 ID: ADMIN-02 회원 관리 · ADMIN-03 공지 · ADMIN-04 문의 답변 · ADMIN-05·06 신고 검토·재검토 · ADMIN-07 정지 · ADMIN-10 대시보드 · ADMIN-11 저격왕 전체 글
* 테이블: `admin_audit_logs` (이 패키지 소유). 나머지는 다른 패키지 서비스를 통해서만 읽고 씀
* 화면: [frontend/src/pages/Admin](../../../../../../../frontend/src/pages/Admin/README.md)

## API

| 메서드 | 경로 | 부르는 서비스 | 응답 | 기능 ID |
|---|---|---|---|---|
| GET | `/admin/dashboard` | `AdminDashboardService.getDashboard()` | 200 `DashboardResponse` | ADMIN-10 |
| GET | `/admin/users?keyword&status&page&size` | `AdminUserService.getUsers` | 200 `PageResponse<AdminUserDetailResponse>` | ADMIN-02 |
| GET | `/admin/users/{targetUserId}` | `AdminUserService.getUser` | 200 `AdminUserDetailResponse` | ADMIN-02 |
| PATCH | `/admin/users/{targetUserId}` | `AdminUserService.correctUser` | 204 | ADMIN-02 |
| POST | `/admin/users/{targetUserId}/suspension` | `AdminUserService.suspendUser` | 204 | ADMIN-07 |
| DELETE | `/admin/users/{targetUserId}/suspension` | `AdminUserService.unsuspendUser` | 204 | ADMIN-07 |
| POST · PUT · DELETE | `/admin/notices` · `/admin/notices/{noticeId}` | `NoticeService` (support) | 201 · 204 · 204 | ADMIN-03 |
| GET | `/admin/inquiries?status&user_id&page&size` | `InquiryService.getInquiriesForAdmin` (support) | 200 `PageResponse<InquiryResponse>` | ADMIN-04 |
| POST | `/admin/inquiries/{inquiryId}/answer` | `InquiryService.answerInquiry` (support) | 201 | ADMIN-04 |
| GET | `/admin/reports?status&page&size` | `AdminReportService.getReports` | 200 `PageResponse<AdminReportResponse>` | ADMIN-05 |
| PATCH | `/admin/reports/{reportId}` | `AdminReportService.reviewReport` | 204 | ADMIN-05 |
| PATCH | `/admin/pass-stories/{storyId}/re-review` | `AdminReportService.reReviewPassStory` | 204 | ADMIN-06 |
| GET | `/admin/pass-stories?nickname&status&page&size` | `PassStoryService.getPassStoriesForAdmin` (최기완) | 200 `PageResponse<PassStoryCardResponse>` | ADMIN-11 |

경로 앞의 `/api/v1`은 생략했습니다.

## 처리 흐름

### 회원 정보 정정 (ADMIN-02)
1. `UserService.getUser`로 변경 전 값 읽기
2. `UserService.correctProfile`로 저장 (이메일·가입일·누적 숨김 횟수는 정정 대상 아님)
3. `AuditLogService.record(action = UPDATE_PROFILE, reason 필수)`, before·after JSON에 민감정보 넣지 않음

### 정지·해제 (ADMIN-07)
* 정지: `UserService.suspend` → `AuthService.revokeAllTokens` → `AuditLogService.record(SUSPEND)`
* 해제: `UserService.unsuspend` → `AuditLogService.record(UNSUSPEND)`
* 정지 기간 단계(숨김 3회 3일, 4회 7일, 5회 이상 30일)는 서버가 강제하지 않고 화면이 다음 단계를 안내, 관리자가 기간 선택 (2026-11-24 결정)
* 해제 사유가 DB CHECK로 필수인데 API·`UserService.unsuspend`에 사유가 없음 → **N1, 팀 확인 전 구현 보류**

### 신고 검토 (ADMIN-05) · 재검토 (ADMIN-06)

| action | 글 상태 | 신고 상태 | 추가 처리 | 기록 |
|---|---|---|---|---|
| `CONFIRM_HIDE` | `HIDDEN` 유지·확정 | `CONFIRMED` | `UserService.addConfirmedHidden`, `RankService.recalculate`, 사유 필수 | `STORY_HIDE` |
| `DISMISS` | `PUBLISHED`로 복구 | `DISMISSED` | `RankService.recalculate` | `STORY_KEEP` |
| `UNHIDE` (재검토) | `PUBLISHED` | — | `RankService.recalculate` | `STORY_UNHIDE` |
| `DELETE` (재검토) | `DELETED` | — | `RankService.recalculate` | `STORY_DELETE` |

모든 처리에 `PassStoryService.changeStatus`, `ReportService.resolve`, `ReportService.recordModeration` (최기완)을 씁니다. 신고 여러 건 처리 방식(Q7)과 글 작성자 조회(Q5)가 정해진 뒤 구현합니다.

### 대시보드 (ADMIN-10)
`NoticeService.countNotices`, `InquiryService.countUnanswered`, `ReportService.countPending`, `UserService.countUsers`, `QualificationService.countQualifications` 결과를 묶어 반환합니다.

## 관리자 변경 이력 (`AuditLogService.record`)

| action | 언제 | 사유 필수 |
|---|---|---|
| `CREATE` · `UPDATE` · `DELETE` | 공지 등록·수정·삭제 | |
| `ANSWER` | 문의 답변 | |
| `UPDATE_PROFILE` | 회원 정보 정정 | ✅ |
| `SUSPEND` · `UNSUSPEND` | 정지·해제 | ✅ |
| `STORY_HIDE` · `STORY_KEEP` · `STORY_UNHIDE` · `STORY_DELETE` | 신고 검토·재검토 | `STORY_HIDE`만 ✅ |
| `WITHDRAW_USER` | 관리자 강제 탈퇴 (현재 API 없음) | |

* 사유 필수 검사는 서비스에서 합니다 (DB는 검사 안 함)
* `request_id`·`client_ip`는 Q10 결정 전까지 `null`로 저장

## 이 패키지에서 고치는 파일

| 파일 | 할 일 | Phase |
|---|---|---|
| `entity/AdminAuditLog.java` | 생성자 추가 | 2 |
| `service/AuditLogService.java` | `record` 구현 | 2 |
| `service/AdminDashboardService.java` | `getDashboard` 구현 | 4 |
| `service/AdminUserService.java` | 메서드 5개 구현 | 4 |
| `service/AdminReportService.java` | 메서드 3개 구현 | 4 |
| `repository/AdminAuditLogRepository.java` | 필요 시 조회 메서드 추가 | 4 |

테스트: `backend/src/test/java/com/certsniper/admin/service/` 아래 서비스별 테스트, 다른 패키지 서비스는 Mock (Phase 4)

## 확인이 필요한 것

| 번호 | 내용 |
|---|---|
| N1 | 정지 해제 사유: DB CHECK는 필수인데 API·`UserService.unsuspend`에 사유 없음 |
| Q3 | 관리자 문의 검색 유형 필터, 관리자 문의 상세 API |
| Q4 | 신고 상세·VIEW-19에서 글 본문을 볼 API 없음 |
| Q5 | 신고 처리 시 글 작성자 ID·자격증명 조회 방법 없음 |
| Q6 | 재검토 대기 목록, 수정 전후 비교(`previous_content`) API 없음 |
| Q7 | 한 글에 신고가 여러 건일 때 처리 방식과 숨김 횟수 증가 기준 |
| Q9 | 회원 상세에 등급·보유 자격증 없음 (`AdminUserDetailResponse`) |
| Q10 | 변경 이력의 `request_id`·`client_ip`를 채울지 |
| S1 | 회원별 정정 이력 조회 API 없음 |
| S2 | 재검토 후 작성자 안내 방법 없음 |
| S3 | 신고 목록에 신고자 닉네임·신고 대상 표시 정보 없음 |
