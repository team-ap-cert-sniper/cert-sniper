# 📁 Admin (관리자) [황정규]

VIEW-14 ~ VIEW-19, 경로 `/admin/**` (`AdminRoute` 아래, `AdminLayout`이 감쌈)

## 파일

| 파일                     | 경로                  | 역할                                          | 기능 ID     |
| ------------------------ | --------------------- | --------------------------------------------- | ----------- |
| `AdminLayout.jsx`        | —                     | 왼쪽 메뉴 · 사이트로 이동 · 로그아웃 (완성됨) |             |
| `AdminDashboardPage.jsx` | `/admin`              | 건수 카드 5개, 카드 누르면 해당 관리 화면     | ADMIN-10    |
| `AdminUserPage.jsx`      | `/admin/users`        | 회원 검색·상세·정보 정정·정지·해제            | ADMIN-02·07 |
| `AdminNoticePage.jsx`    | `/admin/notices`      | 공지 목록·등록·수정·삭제                      | ADMIN-03    |
| `AdminInquiryPage.jsx`   | `/admin/inquiries`    | 문의 목록·상세·답변                           | ADMIN-04    |
| `AdminReportPage.jsx`    | `/admin/reports`      | 신고 목록·검토, 재검토                        | ADMIN-05·06 |
| `AdminPassStoryPage.jsx` | `/admin/pass-stories` | 저격왕 전체 글 조회                           | ADMIN-11    |

한 화면 전용 컴포넌트가 생기면 `pages/Admin/components/`에 둡니다.

## 쓰는 API (`@/api/adminApi`)

| 화면     | 함수                                                                                                      |
| -------- | --------------------------------------------------------------------------------------------------------- |
| 대시보드 | `getDashboard`                                                                                            |
| 회원     | `getUsers` · `getUser` · `correctUser` · `suspendUser` · `unsuspendUser`                                  |
| 공지     | `createNotice` · `updateNotice` · `deleteNotice` + 목록은 `@/api/supportApi`의 `getNotices` · `getNotice` |
| 문의     | `getInquiriesForAdmin` · `answerInquiry`                                                                  |
| 신고     | `getReports` · `reviewReport` · `reReviewPassStory`                                                       |
| 저격왕   | `getPassStoriesForAdmin`                                                                                  |

## 화면 이동

| 화면      | 누르는 곳                                         | 가는 곳                                                                   |
| --------- | ------------------------------------------------- | ------------------------------------------------------------------------- |
| 대시보드  | 공지 · 미답변 문의 · 미처리 신고 · 전체 회원 카드 | `/admin/notices` · `/admin/inquiries` · `/admin/reports` · `/admin/users` |
| 대시보드  | 등록 자격증 카드                                  | 이동 없음                                                                 |
| 회원 상세 | 작성 글 조회 · 1:1 문의 조회                      | `/admin/pass-stories?nickname=` · `/admin/inquiries?user_id=`             |
| 회원 상세 | 정지 · 해제 · 정보 정정 저장                      | 확인 팝업 → 같은 회원 상세                                                |
| 공지      | 등록 · 수정 · 삭제 완료                           | 공지 목록                                                                 |
| 문의      | 제목 → 답변 저장                                  | 상세 → 같은 상세 (답변완료)                                               |
| 신고      | 처리 완료                                         | 신고 목록 (보던 페이지 유지)                                              |
| 저격왕    | 게시글                                            | 관리자 화면 안 상세 (일반 상세 페이지로 이동 안 함)                       |

## 화면 규칙

- 목록은 5개씩, `@/components/common/Pagination` 사용
- 공지 목록은 작성일만 표시 (작성자 표시 안 함)
- 사유가 필요한 처리(정보 정정, 정지, 해제, 숨김 확정)는 사유 입력칸을 비우면 저장 버튼 비활성
- 정지 팝업은 누적 숨김 횟수(`confirmed_hidden_count`)로 다음 정지 단계(3회 3일, 4회 7일, 5회 이상 30일)를 안내만 하고 기간은 관리자가 선택
- 등급 표시는 `@/components/passstory/RankBadge`, 숨김 안내는 `HiddenNotice` (최기완) 사용
- 스타일은 최소한만 (디자인 통일 전까지 기본 태그 위주)

## 확인이 필요한 것

[backend admin README](../../../../backend/src/main/java/com/certsniper/admin/README.md#확인이-필요한-것)의 N1, Q3~Q7, Q9, S1~S3가 정해지기 전에는 해당 화면 부분을 비워 두고 목록·기본 처리부터 만듭니다.
