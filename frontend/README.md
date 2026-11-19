# 🎨 frontend (React + Vite)

기능 명세서 v1.4.0의 화면 ID(VIEW-01~19) 기준 구조와 **만들 파일 전체**입니다. 화면 하나가 `pages/` 폴더 하나입니다. [← 처음으로](../README.md)

* 기능 ID → 화면 → API → 테이블: [docs/TRACEABILITY.md](../docs/TRACEABILITY.md)
* 화면 흐름: [docs/FLOWCHART.md](../docs/FLOWCHART.md)
* 표시: `★` 화면 개발 전에 먼저 만들 공통 파일 (`feature/common/...`), `[이름]` 담당, 오른쪽 끝은 기능 ID

## 👤 화면 담당

| 담당 | 화면 (`pages/`) | 부품 (`components/`) | API 파일 (`api/`) |
|---|---|---|---|
| 유다연 | `Main/` VIEW-01, `Auth/` VIEW-02~04 | — | `authApi.js` |
| 이은아 | `Recommend/` VIEW-05, `QualificationDetail/` VIEW-06 | `qualification/` | `optionApi.js` · `recommendationApi.js` · `qualificationApi.js` · `examScheduleApi.js` |
| 박준성 | `Planner/` VIEW-07, `AiPlanner/` VIEW-08 | `planner/` | `targetExamApi.js` · `plannerApi.js` · `memoApi.js` · `aiPlannerApi.js` |
| 최기완 | `PassStory/` VIEW-09·10, `MyPage/` VIEW-11 | `passstory/` | `userApi.js` · `favoriteApi.js` · `passStoryApi.js` · `reportApi.js` · `rankApi.js` |
| 황정규 | `Support/` VIEW-12·13, `Admin/` VIEW-14~19 | — | `supportApi.js` · `adminApi.js` |
| 공통 (담당 미정) | `App.jsx` · `router/` · `store/` · `hooks/` · `utils/` · `constants/` · `styles/` | `common/` | `client.js` |

> 담당은 임시 배정입니다. 바뀌면 이 표와 [CODEOWNERS](../.github/CODEOWNERS)를 같이 고칩니다.

## 📁 파일 트리

### 1. 프로젝트 설정 + 공통

```
📁 frontend/
├─ 📄 README.md                         (있음) 화면 구조·경로·이름 규칙
├─ 📄 package.json                    ★ react, react-router-dom, axios, chart.js, react-chartjs-2
├─ 📄 vite.config.js                  ★ 개발 서버 프록시 (/api → 백엔드)
├─ 📄 index.html                      ★
├─ 📄 .env.example                    ★ VITE_API_BASE_URL
├─ 📄 .eslintrc.cjs                     (선택) 코드 규칙
├─ 📁 public/
│  └─ 📁 images/                        로고·등급 아이콘·조준경 아이콘
└─ 📁 src/
   ├─ 📄 main.jsx                     ★ 앱 시작
   ├─ 📄 App.jsx                      ★ 공통 헤더·푸터 + 라우터
   ├─ 📁 api/
   │  └─ 📄 client.js                 ★ Axios: 주소(.env), 토큰 첨부, 401 → 로그인 필요 팝업
   ├─ 📁 router/
   │  ├─ 📄 index.jsx                 ★ 전체 경로
   │  ├─ 📄 ProtectedRoute.jsx        ★ 비회원이면 로그인 필요 팝업                      CF-00-03
   │  └─ 📄 AdminRoute.jsx            ★ ADMIN이 아니면 접근 불가
   ├─ 📁 store/
   │  └─ 📄 authStore.js              ★ 로그인 회원 정보·토큰
   ├─ 📁 hooks/
   │  ├─ 📄 useAuth.js                ★
   │  └─ 📄 useDebounce.js            ★ 검색 입력 지연
   ├─ 📁 utils/
   │  ├─ 📄 date.js                   ★ 날짜 형식·D-Day 문자열
   │  └─ 📄 format.js                 ★ 숫자·% 형식
   ├─ 📁 constants/
   │  ├─ 📄 grades.js                 ★ 학년 (1~6학년·졸업·비재학)
   │  ├─ 📄 qualification.js          ★ 자격구분·등급
   │  ├─ 📄 rank.js                   ★ 저격 등급·아이콘
   │  ├─ 📄 report.js                 ★ 신고 사유 5개
   │  ├─ 📄 inquiry.js                ★ 문의 유형 5개
   │  └─ 📄 quotes.js                 ★ 사자성어 목록                                   CF-04-19
   ├─ 📁 styles/
   │  ├─ 📄 global.css                ★
   │  └─ 📄 variables.css             ★ 색·글꼴·간격
   └─ 📁 components/common/           [공통 · 담당 미정]
      ├─ 📄 Header.jsx                ★ 로고·메뉴·로그인/마이페이지·로그아웃 확인 (ADMIN은 관리자 버튼, 위치 팀 확인)        CF-00-01
      ├─ 📄 Footer.jsx                ★ 고객센터·1:1 문의 바로가기
      ├─ 📄 LoginRequiredModal.jsx    ★ 로그인 필요 팝업                                 CF-00-03
      ├─ 📄 ConfirmModal.jsx          ★ 로그아웃·삭제·작성 취소 확인
      ├─ 📄 AlertModal.jsx            ★ 완료·안내 팝업 (가입 완료·비밀번호 변경·탈퇴 완료 → 버튼으로 이동)  AUTH-01·04·05·06
      ├─ 📄 SearchSelect.jsx          ★ 검색 + 선택 (대학교·전공·자격증)                 CF-01-01
      ├─ 📄 ChipSelect.jsx            ★ 최대 5개 칩 (추천 조건의 희망 직무·관심 분야)     CF-01-02
      ├─ 📄 EmailVerifyField.jsx      ★ 이메일 + 인증코드 + 3분 카운트다운 (가입·재설정·변경)  AUTH-01·05·06
      ├─ 📄 Pagination.jsx            ★ 5개 단위 페이지 블록                             CF-05-11
      ├─ 📄 FieldError.jsx            ★ 입력칸 오류 문구
      ├─ 📄 EmptyState.jsx            ★ 0건 안내 (추천: 학교 정보 없음 · 학과 연결 없음 · 조건 결과 없음)
      └─ 📄 Loading.jsx               ★ 로딩
```

### 2. API 파일 (backend 패키지 하나에 하나)

```
📁 src/api/
├─ 📄 authApi.js                        auth  [유다연]
├─ 📄 userApi.js                        user  [최기완]
├─ 📄 optionApi.js                      option  [이은아]
├─ 📄 recommendationApi.js              recommendation  [이은아]
├─ 📄 qualificationApi.js               qualification (상세·트렌드)  [이은아]
├─ 📄 examScheduleApi.js                examschedule  [이은아]
├─ 📄 favoriteApi.js                    favorite  [최기완]
├─ 📄 targetExamApi.js                  targetexam  [박준성]
├─ 📄 plannerApi.js                     planner  [박준성]
├─ 📄 memoApi.js                        memo  [박준성]
├─ 📄 aiPlannerApi.js                   aiplanner  [박준성]
├─ 📄 passStoryApi.js                   passstory  [최기완]
├─ 📄 reportApi.js                      report  [최기완]
├─ 📄 rankApi.js                        rank  [최기완]
├─ 📄 supportApi.js                     support  [황정규]
└─ 📄 adminApi.js                       admin  [황정규]
```

### 3. 화면 (pages)

```
📁 src/pages/
├─ 📁 Main/ [유다연]                       VIEW-01  /
│  └─ 📄 MainPage.jsx                     슬로건·바로가기·트렌드·오늘의 일정 패널          CF-00-02·04
├─ 📁 Auth/ [유다연]                       VIEW-02~04
│  ├─ 📄 SignupPage.jsx                   /signup  ① 이메일·인증·비밀번호 → ② 이름·닉네임 → ③ 학력(없으면 비활성)·보유 자격증(선택)  AUTH-01
│  ├─ 📄 LoginPage.jsx                    /login                                        AUTH-02
│  └─ 📄 PasswordResetPage.jsx            /password-reset                               AUTH-05
├─ 📁 Recommend/ [이은아]                  VIEW-05  /recommend
│  └─ 📄 RecommendPage.jsx                조건 바·추천 캐러셀(최대 5개)·트렌드 1~3위·합격수기 최신순, 새로고침 시 전공 맞춤  CF-01-01~12
├─ 📁 QualificationDetail/ [이은아]        VIEW-06  /qualifications · /qualifications/:jmcd
│  └─ 📄 QualificationDetailPage.jsx      검색 첫 화면 / 선택 자격증 상세                  CF-02-01~10
├─ 📁 Planner/ [박준성]                    VIEW-07  /planner
│  └─ 📄 PlannerPage.jsx                  드롭다운·타임라인·작은 캘린더·메모·모드          CF-04-01~19
├─ 📁 AiPlanner/ [박준성]                  VIEW-08  /ai-planner?jmcd=
│  ├─ 📄 AiPlannerPage.jsx                진입 검사·단계 이동·로딩·저장 후 이동            CF-06-01~05
│  └─ 📄 SurveyStep.jsx                   설문 한 단계 (7단계 공통 틀)                     CF-06-02
├─ 📁 PassStory/ [최기완]                  VIEW-09~10
│  ├─ 📄 PassStoryListPage.jsx            /pass-stories  등급 대시보드·목록·검색·내 글      CF-05-01~04·11·12
│  ├─ 📄 PassStoryDetailPage.jsx          /pass-stories/:id  상세·신고·수정·삭제           CF-05-05·08·09
│  └─ 📄 PassStoryFormPage.jsx            /pass-stories/new · /:id/edit                  CF-05-06·07
├─ 📁 MyPage/ [최기완]                     VIEW-11  /mypage?tab=
│  ├─ 📄 MyPage.jsx                       탭 틀
│  ├─ 📄 ProfileTab.jsx                   회원정보 조회·수정(학력·보유 자격증, 직무·관심 없음)  AUTH-03
│  ├─ 📄 PasswordTab.jsx                  비밀번호 변경 (이메일 인증)                       AUTH-06
│  ├─ 📄 FavoriteTab.jsx                  관심 자격증·AI 플래너 만들기                      CF-03-02·09
│  ├─ 📄 MyPostTab.jsx                    내 작성글·숨김 안내                               CF-05-03·10
│  ├─ 📄 MyReportTab.jsx                  신고당한 내역                                     CF-05-13
│  └─ 📄 WithdrawTab.jsx                  탈퇴·철회                                         AUTH-04
├─ 📁 Support/ [황정규]                    VIEW-12~13  /support?menu=
│  ├─ 📄 SupportPage.jsx                  왼쪽 메뉴 틀                                      CS-01
│  ├─ 📄 NoticeSection.jsx                공지 목록·상세                                    CS-02
│  ├─ 📄 FaqSection.jsx                   질문·답변 펼치기                                  CS-03
│  ├─ 📄 InquiryFormSection.jsx           1:1 문의 (회원)                                   CS-04
│  └─ 📄 MyInquirySection.jsx             나의 문의 내역·상세 (회원)                        CS-05
└─ 📁 Admin/ [황정규]                      VIEW-14~19  /admin/**
   ├─ 📄 AdminDashboardPage.jsx           VIEW-14                                         ADMIN-10
   ├─ 📄 AdminUserPage.jsx                VIEW-15  목록·상세·정정·정지·등급               ADMIN-02·07~09
   ├─ 📄 AdminNoticePage.jsx              VIEW-16                                         ADMIN-03
   ├─ 📄 AdminInquiryPage.jsx             VIEW-17                                         ADMIN-04
   ├─ 📄 AdminReportPage.jsx              VIEW-18  신고 검토·재검토                        ADMIN-05·06
   └─ 📄 AdminPassStoryPage.jsx           VIEW-19                                         ADMIN-11
```

### 4. 기능별 부품 (components)

```
📁 src/components/
├─ 📁 qualification/ [이은아]
│  ├─ 📄 ConditionBar.jsx                 현재 선택 조건 바(회원정보 표시 + 조건 선택)       CF-01-09
│  ├─ 📄 RecommendCarousel.jsx            추천 카드 캐러셀(최대 5개, 후보가 적으면 그 수만큼, 1개면 화살표 숨김)  CF-01-05
│  ├─ 📄 RecommendCard.jsx                카드 (찜·상세·AI 플래너 버튼)                    CF-01-10·11
│  ├─ 📄 ScoreReason.jsx                  추천 근거·항목별 점수                            CF-01-08
│  ├─ 📄 TrendList.jsx                    주목받는 자격증 (메인 · 추천 페이지는 1~3위)      CF-02-08
│  ├─ 📄 QualificationSearchModal.jsx     검색 결과 팝업·필터                              CF-02-09
│  ├─ 📄 QualificationBrowser.jsx         자격구분 → 직무분야 → 분류 → 종목 탐색             CF-02-09
│  ├─ 📄 QualificationInfo.jsx            기본정보(영문명·관련부처·수수료)·시험과목         CF-02-01~02
│  ├─ 📄 EligibilityInfo.jsx              응시 조건 필요/없음 · 조건 목록                   CF-02-03
│  ├─ 📄 PassRateChart.jsx                연도 선택 차트(접수자·응시자·합격자)·합격률 추이   CF-02-05
│  ├─ 📄 PassRateTable.jsx                연도별 상세·종목별 검정현황 표                     CF-02-06
│  ├─ 📄 ExamScheduleTable.jsx            회차별 한 줄(정기·빈자리 접수)·상태·D-n            CF-02-10
│  └─ 📄 DifficultyInfo.jsx               난이도 (유지 여부 미정, 확정되면 생성)               CF-02-07
├─ 📁 planner/ [박준성]
│  ├─ 📄 TodaySchedulePanel.jsx           메인 작은 달력·오늘의 일정                       CF-00-04
│  ├─ 📄 TargetExamDropdown.jsx           목표 자격증 토글                                 CF-04-04
│  ├─ 📄 DdayBadge.jsx                    시험일·D-Day 표시                                CF-04-05·06
│  ├─ 📄 MiniCalendar.jsx                 작은 캘린더                                      CF-04-03
│  ├─ 📄 Timeline.jsx                     타임라인 눈금·세로줄                             CF-04-19
│  ├─ 📄 TimelineBar.jsx                  일정바                                           CF-04-13
│  ├─ 📄 Milestone.jsx                    당일 일정 원형                                   CF-04-15
│  ├─ 📄 ScheduleModal.jsx                자격증 일정 등록·수정                            CF-04-14
│  ├─ 📄 PersonalScheduleModal.jsx        개인 일정                                        CF-04-18
│  ├─ 📄 TargetExamSearchModal.jsx        목표 자격증 검색 추가                            CF-04-09
│  ├─ 📄 MemoModal.jsx                    메모 편집·서식·축소·확장                         CF-04-01·08·17
│  └─ 📄 DailyQuote.jsx                   오늘의 사자성어                                  CF-04-19
└─ 📁 passstory/ [최기완]
   ├─ 📄 PassStoryCard.jsx                카드 (추천 페이지 미리보기에서도 사용)           CF-05-05, CF-01-12
   ├─ 📄 RankDashboard.jsx                등급 대시보드                                    CF-05-04
   ├─ 📄 RankBadge.jsx                    닉네임 옆 등급 아이콘                            CF-05-04
   ├─ 📄 RankUpModal.jsx                  승급 팝업                                        CF-05-12
   ├─ 📄 ReportModal.jsx                  신고 팝업                                        CF-05-09
   └─ 📄 HiddenNotice.jsx                 숨김 안내                                        CF-05-10
```

## 🧩 다른 담당의 부품·API를 쓰는 곳

자기 화면에서 다른 담당의 부품·API 파일을 **가져다 쓰기만** 합니다. 고칠 일이 있으면 담당자에게 요청합니다.

| 쓰는 화면 (담당) | 가져다 쓰는 것 (담당) | 기능 ID |
|---|---|---|
| Main (유다연) | `qualification/TrendList` (이은아), `planner/TodaySchedulePanel` (박준성) | CF-00-02·04, CF-02-08 |
| Auth 회원가입 (유다연) | `optionApi` 대학교·전공·자격증 목록 (이은아), `common/SearchSelect`·`EmailVerifyField` | AUTH-01, CF-01-01·07 |
| Recommend (이은아) | `passstory/PassStoryCard` (최기완), `favoriteApi` 찜 (최기완), `passStoryApi` 미리보기 (최기완) | CF-01-10·12, CF-03-01·02 |
| QualificationDetail (이은아) | — | |
| Planner (박준성) | `optionApi` 종목명 검색 · `examScheduleApi` 회차 (이은아) | CF-03-03, CF-04-09·14 |
| AiPlanner (박준성) | `examScheduleApi` 시험일 회차 · `qualificationApi` 자격증명 (이은아) | CF-06-01·02 |
| PassStory (최기완) | `optionApi` 자격증 검색 · `qualificationApi` 시험과목 (이은아) | CF-05-02·06 |
| MyPage (최기완) | `authApi` 비밀번호 변경 (유다연), `optionApi` 학력·자격증 (이은아), `common/EmailVerifyField` | AUTH-03·06 |
| Support (황정규) | `common/Pagination` | CS-02·05 |
| Admin (황정규) | `passstory/RankBadge`·`HiddenNotice` (최기완), `common/Pagination` | ADMIN-02·05·11 |

## 🧭 화면 경로

| 경로 | 화면 | 접근 | 담당 |
|---|---|---|---|
| `/` | VIEW-01 초기 메인 (로그인 전·후) | 전체 | 유다연 |
| `/signup` · `/login` · `/password-reset` | VIEW-02 · 03 · 04 | 비회원 | 유다연 |
| `/recommend` | VIEW-05 자격증 추천 | 회원 | 이은아 |
| `/qualifications` · `/qualifications/:jmcd` | VIEW-06 상세 첫 화면 · 특정 자격증 상세 | 회원 | 이은아 |
| `/planner` | VIEW-07 플래너 다이어리 | 회원 | 박준성 |
| `/ai-planner?jmcd=` | VIEW-08 AI 플래너 설문 | 회원 | 박준성 |
| `/pass-stories` · `/pass-stories/:id` · `/pass-stories/new` · `/pass-stories/:id/edit` | VIEW-09 · 10 | 회원 | 최기완 |
| `/mypage?tab=profile·password·favorites·posts·reports·withdraw` | VIEW-11 마이페이지 탭 6개 | 회원 | 최기완 |
| `/support?menu=notices·faq` · `/support?menu=inquiry·my-inquiries` | VIEW-12 · 13 | 공지·FAQ 전체 / 문의 회원 | 황정규 |
| `/admin` · `/admin/users` · `/admin/notices` · `/admin/inquiries` · `/admin/reports` · `/admin/pass-stories` | VIEW-14~19 | 관리자 | 황정규 |

## 🔀 화면 이동 (전체)

`팀 확인`은 명세서에 이동할 곳이 적혀 있지 않아 정해야 하는 연결입니다.

| 화면 | 누르는 곳 | 가는 곳 | 기능 ID |
|---|---|---|---|
| 공통 헤더 | 로고 | `/` | CF-00-01 |
| 공통 헤더 | 자격증 상세 · 자격증 추천 · 플래너 다이어리 · 저격왕 | `/qualifications` · `/recommend` · `/planner` · `/pass-stories` | CF-00-01 |
| 공통 헤더 | 로그인 · 회원가입 (비회원) | `/login` · `/signup` | CF-00-01 |
| 공통 헤더 | 마이페이지 (회원) | `/mypage` | CF-00-01 |
| 공통 헤더 | 로그아웃 → 확인 팝업 | `/` | CF-00-01, AUTH-02 |
| 공통 헤더 | 관리자 버튼 (ADMIN) | `/admin` (버튼 위치 팀 확인) | ADMIN-10 |
| 공통 | 비회원이 회원 전용 메뉴·버튼·URL | 로그인 필요 팝업 → 로그인하러 가기 `/login` · 취소하기 제자리 | CF-00-03 |
| 공통 푸터 | 고객센터 바로가기 · 1:1 문의하기 | `/support` · `/support?menu=inquiry` (비회원은 로그인 필요 팝업) | CF-00-02, CS-04 |
| VIEW-01 | 맞춤 자격증 추천하기 · 플래너 다이어리 · 합격수기 | `/recommend` · `/planner` · `/pass-stories` | CF-00-02 |
| VIEW-01 | 트렌드 상세보기 · 자격증 전체보기 | `/qualifications/:jmcd` · `/qualifications` | CF-02-08 |
| VIEW-01 | 오늘의 일정 패널 열기·닫기 | 이동 없음 (수정은 `/planner`에서만) | CF-00-04 |
| VIEW-02 | 회원가입 완료 팝업 로그인하러 가기 | `/login` | AUTH-01 |
| VIEW-03 | 비밀번호 재설정 | `/password-reset` | AUTH-05 |
| VIEW-03 | 로그인 성공 | `/` 또는 로그인 필요 팝업을 띄운 화면 (팀 확인) | AUTH-02 |
| VIEW-04 | 비밀번호 변경 완료 팝업 로그인하러 가기 | `/login` | AUTH-05 |
| VIEW-05 | 카드 자격증 상세보기 | `/qualifications/:jmcd` | CF-01-06 |
| VIEW-05 | 카드 찜하기 (조준경) | 이동 없음, 찜 등록·해제 → 마이페이지 관심 자격증 | CF-03-01·02 |
| VIEW-05 | 카드 AI 플래너 생성하기 | `/ai-planner?jmcd=` | CF-01-11 |
| VIEW-05 | 트렌드 상세보기 · 전체 자격증 보기 | `/qualifications/:jmcd` · `/qualifications` | CF-01-12, CF-02-08 |
| VIEW-05 | 합격수기 보러 가기 | `/pass-stories` | CF-01-12 |
| VIEW-05 | 페이지를 나갔다 오기 · 새로고침 | 같은 화면, 전공 맞춤 추천으로 처음부터 (고른 조건 저장 안 함) | CF-01-05 |
| VIEW-05 | 학교 정보 없는 회원 진입 | 같은 화면, 관심 분야·희망 직무 선택 안내 + 조건 검색창 펼침 | CF-01-05 |
| VIEW-05 | 전공 맞춤 결과 0개 (학과 연결 자격 없음) | 같은 화면, 관심 분야·희망 직무로 찾아보기 안내 + 조건 검색창 펼침 | CF-01-05 |
| VIEW-05 | 조건 선택 결과 0개 | 같은 화면, 조건 변경 안내 (고른 조건 유지) | CF-01-05, CF-01-09 |
| VIEW-06 | 검색하기 → 결과 팝업에서 선택 · 탐색 목록 종목 선택 · 트렌드 상세보기 | 같은 화면 아래 상세 (`/qualifications/:jmcd`) | CF-02-08·09 |
| VIEW-06 | 응시자격 안내 링크 | Q-Net 응시자격 안내 (새 창) | CF-02-03 |
| VIEW-07 | 목표 자격증 추가 · 일정 연필 · 개인 일정 연필 · 메모 | 같은 화면 팝업 | CF-04-01·07·09·14·17·18 |
| VIEW-07 | 플래너 직접 수정하기 | 같은 화면 직접 수정 상태 | CF-04-12 |
| VIEW-08 | 일정 6개 초과 (새 자격증) | 설문 시작 전 안내 | CF-06-01 |
| VIEW-08 | 플랜 저장 완료 | `/planner` (AI 렌더링 상태) | CF-06-05 |
| VIEW-09 | 카드 | `/pass-stories/:id` | CF-05-05 |
| VIEW-09 | 글쓰기 | `/pass-stories/new` (버튼 위치 팀 확인) | CF-05-06 |
| VIEW-09 | 전체보기 · 내가 쓴 글 보기 | 같은 화면 목록 전환 | CF-05-03 |
| VIEW-09 | 등록 후 등급이 오르면 | 승급 팝업 → 확인 | CF-05-12 |
| VIEW-10 | 상세 수정 · 삭제 (본인) | `/pass-stories/:id/edit` · 확인 팝업 → `/pass-stories` | CF-05-07·08 |
| VIEW-10 | 상세 신고하기 (타인) | 신고 팝업 → 즉시 숨김 | CF-05-09 |
| VIEW-10 | 목록으로 돌아가기 · 작성 취소 확인 | `/pass-stories` · 이전 화면 | CF-05-05·06·07 |
| VIEW-10 | 등록 완료 | `/pass-stories` | CF-05-06·12 |
| VIEW-11 | 회원정보 보유 자격증 | `/qualifications/:jmcd` | AUTH-03 |
| VIEW-11 | 관심 자격증 상세보기 · AI 플래너 만들기 | `/qualifications/:jmcd` · `/ai-planner?jmcd=` | CF-03-09 |
| VIEW-11 | 내 작성글 · 숨김 글 수정 | `/pass-stories/:id` · `/pass-stories/:id/edit` | CF-05-03·10 |
| VIEW-11 | 회원 탈퇴 완료 팝업 | `/` (로그아웃, 팀 확인) | AUTH-04 |
| VIEW-12 | 왼쪽 메뉴 공지사항 · FAQ · 1:1 문의하기 · 나의 문의 내역 | 같은 화면 내용 전환 (문의 메뉴는 비회원이면 로그인 필요 팝업) | CS-01 |
| VIEW-12 | 공지 제목 · FAQ 질문 | 공지 상세 · 답변 펼침 | CS-02·03 |
| VIEW-13 | 문의 접수 | 나의 문의 내역 (팀 확인) | CS-04 |
| VIEW-13 | 문의 제목 · 목록으로 | 문의 상세 · 목록 | CS-05 |
| VIEW-14 | 공지 · 미답변 문의 · 미처리 신고 · 전체 회원 카드 | `/admin/notices` · `/admin/inquiries` · `/admin/reports` · `/admin/users` | ADMIN-10 |
| VIEW-14 | 등록 자격증 카드 | 자격증 관리 화면 없음 → 숫자만 표시 또는 `/qualifications` (팀 확인) | ADMIN-10 |
| VIEW-15 | 회원 상세 작성 글 조회 · 1:1 문의 조회 | `/admin/pass-stories?nickname=` · `/admin/inquiries?userId=` | ADMIN-02 |
| VIEW-15 | 정지 · 해제 · 등급 보정 · 재산정 | 같은 화면 팝업 | ADMIN-07~09 |
| VIEW-17 | 문의 제목 | 문의 상세 → 답변 저장 | ADMIN-04 |
| VIEW-18 | 신고 상세 · 재검토 | 숨김 확정·해제 · 숨김 해제·삭제 | ADMIN-05·06 |
| VIEW-19 | 게시글 | 게시글 조회 (신고·숨김 상태 표시) | ADMIN-11 |

## 이름 규칙

| 대상 | 규칙 | 예시 |
|---|---|---|
| 화면 폴더 | 대문자로 시작 | `Recommend/`, `PassStory/` |
| 화면 파일 | `~Page.jsx` | `RecommendPage.jsx` |
| 부품 파일 | 대문자로 시작, 역할이 보이게 | `PassRateChart.jsx` |
| API 파일 | 소문자로 시작, `~Api.js` | `qualificationApi.js` |
| 훅 | `use`로 시작 | `useAuth.js` |
| 경로 | 소문자, 단어 사이 `-` | `/pass-stories`, `/ai-planner` |

## 지켜 주세요

* 다른 기능의 화면·부품을 고치지 않습니다. 공통 부품이 필요하면 `components/common/`에 새로 만들고 PR에 적어 주세요
* 백엔드 주소 같은 설정은 `.env`에 두고, 공유할 예시는 `.env.example`로 올립니다
* 디자인 시안은 임시입니다. 화면 배치보다 기능·이동을 먼저 맞춥니다
