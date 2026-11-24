# 📁 Support (고객센터) [황정규]

VIEW-12 · VIEW-13, 경로 `/support?menu=` (회원 전용, `ProtectedRoute` 아래)

## 파일

| 파일                                | 역할                                              | 기능 ID  |
| ----------------------------------- | ------------------------------------------------- | -------- |
| `SupportPage.jsx`                   | 왼쪽 메뉴 틀, `menu`·`id`·`page` 쿼리로 섹션 전환 | CS-01    |
| `components/NoticeSection.jsx`      | 공지 목록(5개씩)·상세                             | CS-02    |
| `components/FaqSection.jsx`         | 질문·답변 펼치기 (고정 문구)                      | CS-03    |
| `components/InquiryFormSection.jsx` | 1:1 문의 작성                                     | CS-04    |
| `components/MyInquirySection.jsx`   | 나의 문의 목록(5개씩)·상세                        | CS-05    |
| `components/faqData.js`             | FAQ 질문·답변 고정 문구                           | CS-03    |
| `supportShared.js`                  | 목록 크기(5), 공지 유형 표시명, 오류 문구 헬퍼    | CS-02·05 |

## 쿼리 파라미터

| 값                                       | 화면                           |
| ---------------------------------------- | ------------------------------ |
| `?menu=notices` (기본)                   | 공지 목록                      |
| `?menu=notices&page=2`                   | 공지 목록 3번째 페이지 (0부터) |
| `?menu=notices&id=7`                     | 공지 상세                      |
| `?menu=faq`                              | FAQ                            |
| `?menu=inquiry`                          | 1:1 문의 작성                  |
| `?menu=my-inquiries` · `&id=` · `&page=` | 나의 문의 목록·상세            |

상세에서 목록으로 돌아갈 때 `page`를 유지합니다 (브라우저 뒤로가기 아님).

## 쓰는 API (`@/api/supportApi`)

| 함수                                              | 섹션           | 비고                                     |
| ------------------------------------------------- | -------------- | ---------------------------------------- |
| `getNotices({ page, size: 5 })`                   | 공지 목록      | 목록 응답의 `content`는 `null`           |
| `getNotice(noticeId)`                             | 공지 상세      |                                          |
| `createInquiry({ inquiry_type, title, content })` | 문의 작성      | 201, 응답 본문 없음 → 공지 목록으로 이동 |
| `getMyInquiries({ page, size: 5 })`               | 나의 문의 목록 |                                          |
| `getMyInquiry(inquiryId)`                         | 나의 문의 상세 | 답변이 있으면 `answer` 표시              |
| `getFaqs`                                         | 쓰지 않음      | FAQ는 `faqData.js` 사용                  |

## 화면 규칙

- 목록은 5개씩, `@/components/common/Pagination` 사용
- 공지 목록·상세는 제목·유형·작성일만 표시 (작성자 표시 안 함)
- 문의 유형·상태 표시명은 `@/constants/inquiry`의 `INQUIRY_TYPES`·`INQUIRY_STATUS` 사용
- 문의 제목 50자, 내용 1,000자 제한 (입력칸 `maxLength` + 남은 글자 수)
- 다른 회원 문의 `id`로 들어오면 서버 오류 메시지 표시
- 스타일은 최소한만 (디자인 통일 전까지 기본 태그 위주)

## 확인이 필요한 것

| 번호 | 내용                                                                                                         |
| ---- | ------------------------------------------------------------------------------------------------------------ |
| Q12  | 공지 유형 표시명(`GENERAL` 일반 · `MAINTENANCE` 시스템 점검)을 어디에 둘지. 결정 전에는 이 폴더 안 상수로 둠 |
