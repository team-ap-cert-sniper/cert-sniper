# kiwan.md — 최기완 파트 작업 노트

담당: 합격수기(VIEW-09·10), 마이페이지(VIEW-11)
패키지: `user` `favorite` `passstory` `report` `rank` / 프론트 `pages/PassStory` `pages/MyPage` `components/passstory`

> 폴더별 README 대신 이 파일에 규칙·진행·미결 사항을 모은다. 파일 트리와 계약은 `backend/README.md`, `frontend/README.md`, `docs/TRACEABILITY.md`가 기준이다.

## 진행 현황

| Phase | 내용 | 상태 |
|---|---|---|
| 1 | rank·favorite·user(일부) 서비스, 등급 테스트 | 완료 (단위 테스트 10개 통과) |
| 2 | 합격수기·신고 백엔드 | 완료, 키워드 검색만 Q1 대기 (단위 테스트 12개 통과, 앱 기동 시 빈 생성 확인) |
| 3 | 마이페이지·탈퇴·남은 user 메서드 | 완료, 탈퇴 정리 배치만 Q9 대기 (단위 테스트 9개 추가, 누적 31개 통과, 앱 기동 확인) |
| 4 | 저격왕 화면 | 완료 (lint·prettier·build 통과, 브라우저 확인은 외부 API 501로 미실시) |
| 5 | 마이페이지 화면·통합 점검 | 완료 (백엔드 테스트 31개·프론트 lint·prettier 전체·build 통과, 실제 화면 확인은 외부 API 501로 미실시) |

## Phase 1 구현 내용

### rank
- 등급: 서로 다른 유효 자격증 수 0→`TRAINEE`, 1~2→`APPRENTICE`, 3~4→`MARKSMAN`, 5+→`LEGEND`
- 유효 글: `PUBLISHED` + `HIDDEN`이면서 `hidden_confirmed_at`이 null (`PassStoryRepository.countValidCerts`)
- `recalculate`: `user_ranks` 행이 없으면 생성. 새 등급이 이전보다 높을 때만 `rankedUp=true`
- 테스트: `backend/src/test/java/com/certsniper/rank/RankServiceTest.java`

### favorite
- `addFavorite`: 이미 찜이면 `DUPLICATE`(409)
- `removeFavorite`: 찜하지 않은 종목이어도 204 (멱등)
- `getFavoriteJmcds`, `deleteAllByUser` 구현. `getFavorites`는 Phase 3 (`QualificationService.getInfo` 필요)

### user (Phase 1 범위)
- 구현: `createUser` `findByEmail` `getUser` `updatePassword` `isNicknameAvailable` `getNicknames` `countUsers`
- `createUser`: 닉네임 중복이면 `DUPLICATE`. 보유 자격은 공백·중복을 제거하고 저장. 학교 정보 검증은 auth가 호출한다고 보고 여기서는 하지 않음
- `findByEmail`: 없으면 null, `getUser`: 없으면 `NOT_FOUND`
- 엔티티·리포지토리에 추가한 것: `User.create` `User.changePassword` `UserCertificate.ofJmcd/ofName` `Favorite.of` `UserRepository.findByEmail/existsByNickname` `FavoriteRepository.existsByUserIdAndJmcd/findByUserId/deleteByUserIdAndJmcd/deleteByUserId` (필드·컬럼 변경 없음)

## Phase 2 구현 내용

### passstory
- 목록 `GET /pass-stories`
  - 전체: `PUBLISHED`만 / `jmcds`: 해당 종목의 `PUBLISHED` / `mine=true`: `DELETED`를 뺀 내 글 전부 (숨김·재검토 대기 포함)
  - 정렬은 `LATEST`(등록 최신순, 같으면 id 역순)만. 다른 값, `page<0`, `size`가 1~50 밖이면 `VALIDATION_ERROR`
  - `keyword`가 있으면 **501** (Q1 결정 전까지)
  - 카드: 닉네임 `UserService.getNicknames`, 등급 `RankService.getCurrentRanks`, 종목명 `QualificationService.getInfo` (같은 종목은 한 번만 호출), 본문 미리보기는 태그를 뺀 텍스트 앞 100자
- 상세: `DELETED`면 `NOT_FOUND`. 타인의 `HIDDEN`·`REVIEW_PENDING` 글은 `body`·`textbook_name`을 null로 반환 (화면은 `status`로 숨김 안내)
- 등록: `getInfo`로 종목 존재 확인 → `HtmlSanitizer` → 저장 → `recalculate` 결과(승급 여부) 반환
- 수정 (작성자만, 타인은 `FORBIDDEN`)
  - `PUBLISHED` → 그대로 공개, 이력 `AUTHOR_EDIT`
  - `HIDDEN`·`REVIEW_PENDING` → `REVIEW_PENDING`, 이력 `RESUBMIT`
  - 수정 뒤에도 `recalculate` (종목 변경·재검토 대기로 유효 수가 바뀔 수 있음. 내려가도 알림 없음)
- 삭제 (작성자만): `DELETED`(소프트 삭제), 이력 `AUTHOR_DELETE`, `recalculate`
- `hideByReport`: `PUBLISHED`일 때만 `HIDDEN` + 사유. 이미 숨김·재검토 대기면 그대로
- `changeStatus` (admin): `HIDDEN` = 숨김 확정(`hidden_confirmed_at` 기록), `PUBLISHED` = 숨김 해제(사유·확정 시각 삭제). 이력과 등급 재계산은 admin이 직접 호출
- 입력 검증: `examinee_base`·`prep_period`·`attempt_band`·`report_type`에 DB CHECK와 같은 `@Pattern`, 합격 점수 `@Digits(4,2)`, 정제 후 본문이 비거나 1500자를 넘으면 `VALIDATION_ERROR` (DTO 필드명은 변경 없음)

### report
- 신고: 삭제된 글 `NOT_FOUND`, 본인 글 `FORBIDDEN`, 같은 글 재신고 `DUPLICATE` → 신고 저장 → `hideByReport` → 공개 글이었으면 이력 `REPORT_HIDE`(actor `REPORTER`)
- `recordModeration` `countPending` `getReportsForAdmin`(status가 null이면 전체) `resolve`(`CONFIRMED`·`DISMISSED`만) 구현
- `PassStoryService` ↔ `ReportService`가 계약상 서로를 호출 → `ReportService` 생성자에서 `@Lazy`로 순환 참조를 끊음
- `ReportService`는 신고 대상 확인용으로 `PassStoryRepository`를 직접 읽음 (두 패키지 모두 내 담당)

### Phase 3으로 넘긴 것 → Phase 3에서 구현 완료
- `getPassStoriesForAdmin`, `getMyReports`

## Phase 3 구현 내용

### 마이페이지 (user)
- `getMyProfile`: 학교 표시명 `OptionService.getSchoolNames`(학교 정보 없으면 호출 안 함), 보유 자격 종목명 `getInfo`, 등급 `RankService.getCurrentRank`
- `updateMyProfile`
  - 닉네임이 바뀌고 다른 회원이 쓰고 있으면 `DUPLICATE`
  - 학력 `OptionService.validateSchoolInfo`, 보유 종목은 `getInfo`로 존재 확인, 직접 입력 자격증명 100자 초과면 `VALIDATION_ERROR`
  - 보유 자격은 통째로 교체 (벌크 삭제 후 재등록. 벌크 삭제라 UNIQUE 충돌 없음)
- `getRecommendationProfile`: 대학·학과·학년 + 보유 종목코드(직접 입력은 제외)

### 탈퇴 (WithdrawalService)
- 신청: 이미 대기 중 `DUPLICATE`, ACTIVE가 아니면 `VALIDATION_ERROR`, 비밀번호 불일치 `VALIDATION_ERROR`("비밀번호가 일치하지 않습니다.") → `WITHDRAWAL_PENDING` + 신청 일시 → 이력 `WITHDRAW_REQUEST` → `AuthService.revokeAllTokens`
- 철회: 대기 중일 때만 → `ACTIVE`, 이력 `WITHDRAW_CANCEL`
- **정리 배치 `deleteExpiredWithdrawals`는 비워 둠 (Q9)**. 스케줄러는 매일 돌지만 아무것도 하지 않음

### 관리자·로그인용 (user)
- `releaseExpiredSuspension`: 정지 기간이 지났으면 `ACTIVE` + 이력 `AUTO_UNSUSPEND`
- `addConfirmedHidden`: 누적 숨김 +1
- `searchUsers`: keyword는 이메일·이름·닉네임 부분 일치, status 일치 (빈 값이면 조건 없음)
- `correctProfile`: 이름·닉네임은 null이면 유지, 학력 4개는 묶음으로 교체(넷 다 null = 학교 정보 없음), 닉네임 중복 `DUPLICATE`
- `suspend`: 종료 일시가 미래이고 사유가 있어야 함, ACTIVE·SUSPENDED만 가능(정지 중이면 기간 덮어씀), 이력 `SUSPEND`
- `unsuspend`: 정지 중일 때만 → `ACTIVE`, 이력 `UNSUSPEND`. DB CHECK상 사유가 필수라 고정 문구 "관리자 정지 해제"를 남김

### 그 외
- `FavoriteService.getFavorites`: 최근 찜한 순, 종목 정보는 `getInfo`
- `ReportService.getMyReports`: 내 글에 들어온 신고, 최신순, 신고자 정보는 반환 안 함
- `PassStoryService.getPassStoriesForAdmin`: 닉네임 부분 일치·상태 필터, 삭제 글 포함, 최신순
- 제재 기준(플로우차트 8장): 누적 숨김 3회 3일 · 4회 7일 · 5회부터 30일 → Phase 5 신고내역 탭 안내 문구로 사용 (정지 실행은 관리자가 직접)

## Phase 4 구현 내용 (프론트 저격왕)

### 공유 컴포넌트 props (Q11 제안안대로 구현 — 이은아·황정규 확인 필요)
| 컴포넌트 | props | 쓰는 곳 |
|---|---|---|
| `components/passstory/PassStoryCard` | `story` = `GET /pass-stories` 응답 `items[]` 한 개 (snake_case 그대로) | 저격왕 목록, 추천 페이지 미리보기(이은아) |
| `components/passstory/RankBadge` | `rank` = 등급 코드 (`TRAINEE` 등) | 카드·상세, 관리자(황정규) |
| `components/passstory/HiddenNotice` | `status`, `hiddenReason` (공개 글이면 아무것도 안 그림) | 상세·수정·마이페이지, 관리자(황정규) |

### 화면
- 목록 `/pass-stories?mine=&q=&page=`: 조회 조건을 URL에 두어 뒤로 가기·새로고침에도 유지. 페이지 크기 5, 검색하면 1페이지부터
- **자격증 이름 검색 (Q1 해결)**: `GET /options/qualifications?query=`(기존 API)로 이름 → 종목코드를 구한 뒤 `GET /pass-stories?jmcds=a,b,…`(기존 파라미터)로 조회. 백엔드 변경 없음. 일치하는 종목은 앞 100개까지 반영(`shortcut` 주석). 검색은 전체보기에서만
- 등록 후 승급이면 목록으로 이동하면서 승급 팝업 (`location.state`)
- 상세: 본인 글이면 수정·삭제(확인 팝업), 타인의 공개 글이면 신고(사유 5개 라디오). 신고 오류 `DUPLICATE` "이미 신고한 글입니다", `FORBIDDEN` "본인 글은 신고할 수 없습니다"
- 작성·수정: 자격증은 `SearchSelect` + 옵션 API, 선택 항목은 `select`, 취소는 "작성 중인 글은 저장되지 않습니다" 확인. 숨김·재검토 대기 글을 수정할 때는 재검토 안내 표시
- 본문 줄바꿈: 서버 `HtmlSanitizer`(jsoup)가 줄바꿈을 공백으로 합치므로, 입력 텍스트를 이스케이프하고 줄바꿈을 `<br>`로 바꿔 전송. 수정할 때는 `<br>` → 줄바꿈으로 되돌림 (`components/passstory/utils.js`). 글자 수는 전송할 HTML 길이 기준으로 표시
- 스타일 없음. className만 붙임 (`rank-badge`, `hidden-notice`, `pass-story-card`, `pass-story-body`, `rank-dashboard`)

## Phase 5 구현 내용 (프론트 마이페이지)

- `/mypage?tab=profile·password·favorites·posts·reports·withdraw`, 없는 tab이면 회원정보
- **회원정보**: 보기 → 수정 전환. 닉네임(2~10자, 중복이면 "이미 사용 중인 닉네임입니다"), 학교 정보 없음 체크 또는 대학교·전공 `SearchSelect` + 학년, 보유 자격증(검색 추가 + 직접 입력 추가 + 삭제). 저장 후 같은 화면에 완료 안내. 이메일은 표시만
- **비밀번호 관리**: `EmailVerifyField`(purpose `PASSWORD_CHANGE`, 이메일은 가입 이메일로 고정) → 현재·새·확인 입력 → `PATCH /auth/password` → 완료 팝업 → 로그아웃 후 `/login`
- **관심 자격증**: 목록·총 개수, 상세보기(`/qualifications/:jmcd`), AI 플래너 만들기(`/ai-planner?jmcd=`), 찜 해제
- **내 작성글**: `mine=true` 목록 5개씩, 숨김·재검토 대기 안내 (사유는 상세에서 확인)
- **신고내역**: 신고일·자격증·사유·처리 상태 표, 제재 기준 안내, 0건이면 "신고 당한 내역이 없습니다."
- **회원 탈퇴**: 유의사항 → 현재 비밀번호 → 확인 팝업 → 완료 팝업 → 로그아웃 후 `/`. 탈퇴 대기 중이면 신청일·삭제 예정일과 철회 버튼
- `authStore.logout()`은 호출만 함 (공통 저장소 수정 없음)

## 통합 점검 결과 (Phase 5)

| 항목 | 결과 |
|---|---|
| 백엔드 컴파일·전체 테스트 | 통과 (31개) |
| 백엔드 기동 (빈 생성·JPA validate·JPQL 해석) | 통과 (임시 포트 8099로 확인 후 종료) |
| 프론트 `npm run lint` | 통과 (0건) |
| 프론트 `prettier --check src` | 통과 (내 파일만 `--write` 실행) |
| 프론트 `npm run build` | 통과 |
| 실제 화면·API 끝까지 확인 | **미실시**: 로그인(유다연)·`getInfo`·옵션 API(이은아)가 501 |
| 공통 파일 수정 | 없음 (설정·라우터·전역 상태·공통 컴포넌트·`global`·docs 미수정) |

### 내가 정한 기본값 (팀 답변이 다르면 수정)
| 질문 | 적용한 기본값 |
|---|---|
| Q5 정렬·크기 | `LATEST`만, 크기는 프론트가 `size=5` 전달 (컨트롤러 기본값 10은 그대로) |
| Q6 노출 | 목록은 공개 글만, 타인 숨김 글 상세는 본문 없이, 삭제 글은 404 |
| Q7 수정 | 종목 변경 허용, 수정 후 등급 재계산 |
| Q10 오류 코드 | 본인 글 신고 `FORBIDDEN`, 중복 신고 `DUPLICATE`, 탈퇴 비밀번호 불일치 `VALIDATION_ERROR` + 전용 메시지 (기존 코드 재사용, `ErrorCode` 추가 없음) |
| 관리자 메서드 의미 (황정규 확인 필요) | `searchUsers` keyword 대상 = 이메일·이름·닉네임, `correctProfile` 학력은 항상 묶음 교체, 전체 글 목록은 삭제 글 포함 |

## 남은 일 한눈에 보기

| 항목 | 막힌 이유 | 풀리면 할 일 |
|---|---|---|
| 합격수기·회원정보·찜 목록·신고내역 실제 동작 | `getInfo`·`getSchoolNames`·`validateSchoolInfo`·옵션 API 501 (이은아) | 코드 수정 없음, 화면에서 흐름 확인 |
| 탈퇴 신청 | `revokeAllTokens` 501 (유다연) | 코드 수정 없음 |
| 탈퇴 정리 배치 | Q9 비식별 규칙, Q4 `deleteAllByUser` (박준성) | `WithdrawalService.deleteExpiredWithdrawals` 구현 |
| 학력 저장 규칙 | Q12 | 필요하면 `schoolIds` 수정 |
| 화면 디자인 | 팀 통일 예정 | className 기준으로 스타일 적용 |

## 다른 담당 의존

| 부르는 것 | 담당 | 용도 | 상태 |
|---|---|---|---|
| `QualificationService.getInfo` | 이은아 | 찜 목록·합격수기·회원정보 보유 자격·신고내역 종목명 | 501 |
| `OptionService.validateSchoolInfo` · `getSchoolNames` | 이은아 | 회원정보 수정·표시 | 501 |
| `AuthService.revokeAllTokens` | 유다연 | 탈퇴·비밀번호 변경 | 501 |
| `TargetExamService`·`PlannerService.deleteAllByUser` | 박준성 | 탈퇴 확정 | 501 |

## 미결 사항 (팀 확인 대기)

- Q3 유다연: `createUser`에서 닉네임 중복 외 검증을 더 해야 하는가 (현재는 닉네임만)
- 찜 등록 시 존재하지 않는 `jmcd`를 막을지 (`favorites`에 FK가 없고 `getInfo`가 501이라 현재는 검사 안 함)
- ~~Q1~~ 해결: 프론트에서 옵션 API(이름 → jmcd) + `?jmcds=`로 검색. 백엔드 `keyword` 파라미터는 501 그대로 (쓰지 않음)
- **이은아**: 저격왕 화면이 `GET /options/qualifications`(`OptionService.getOptions`)를 검색·작성에 사용함. 지금은 501이라 검색과 자격증 선택이 동작하지 않음
- **Q2 이은아**: `getInfo`가 501이라 합격수기 목록·상세·등록·수정이 지금은 모두 501. 구현 일정은?
- **Q9 이은아·기획 (탈퇴 정리 배치 차단)**: 7일 경과 계정의 비식별 규칙. `email`·`nickname`은 UNIQUE, `name`은 2~4자 CHECK인데 각각 무엇으로 바꾸는가? `user_certificates`·`user_ranks`·`favorites`를 삭제하는가? 합격수기 작성자 표시는 어떻게 하는가?
- **Q4 박준성**: `TargetExamService`·`PlannerService.deleteAllByUser` 구현 일정 (탈퇴 정리 배치에서 사용)
- Q11 공유 컴포넌트 props: 제안안대로 구현 (위 Phase 4 표). 이은아·황정규 확인 필요 (Q8은 플로우차트에서 제재 기준을 확인해 해결)
- **Q12 이은아·유다연 (학력 저장 규칙)**: 전공 옵션 항목(`scope` MAJOR/FACULTY)을 `users.college_id`·`major_id`에 어떻게 나눠 저장하는가? 현재 기본값은 FACULTY → `college_id`, MAJOR → `major_id`(college_id는 null). 회원가입(유다연)과 같은 규칙이어야 함 (`ProfileTab.jsx`의 `schoolIds` 한 곳만 고치면 됨)
- **`UserProfileResponse`에 학부명이 없음**: 학부 전체(FACULTY)로 저장한 회원은 `major_name`이 비어 학력에 학부명이 안 보임. `college_name` 필드 추가가 필요하면 DTO 변경 합의 필요
- 탈퇴 대기 계정의 이용 범위: 현재 필터는 `WITHDRAWN`만 차단해서, 대기 중에도 로그인하면 모든 기능을 쓸 수 있음 (철회하려면 로그인이 필요하니 의도일 수 있음) → 유다연·이은아 확인

## 안건 대기 (Phase 2에서 추가)

- **수정 전 내용 이력(`previous_content`)**: `ReportService.recordModeration` 계약 시그니처에 내용 파라미터가 없어서 저장하지 못함 (현재 null). 필요하면 오버로드를 추가하거나 시그니처를 바꿔야 함 → admin(황정규) 합의 필요

## 안건 대기

- **기능 추적표 갱신**: `report`가 `QualificationService.getInfo`를 호출하게 됨 (신고내역 종목명). `backend/README.md` "패키지끼리 부르는 곳" 표의 호출 패키지에 report 추가 필요 (문서는 직접 수정 안 함)
- 코드→한글 라벨 위치: `components/passstory/labels.js`(내 폴더)로 진행, 공통 `constants/` 반영은 이은아 승인 필요
- **화면 문구 확정**: 응시자 베이스·준비 기간·응시 횟수·신고 사유의 한글 문구는 임시로 정함 (`labels.js`). 기획 문구 확인 필요
- **`SearchSelect`에 id/aria 전달 기능** (이은아): label을 input에 연결할 수 없어 접근성 부족. `id` prop 추가 요청
