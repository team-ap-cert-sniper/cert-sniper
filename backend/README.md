# 🖥 backend (Spring Boot)

패키지 하나가 기능 하나입니다. 엔티티·리포지토리·DTO·컨트롤러 URL·서비스 메서드 이름까지 만들어져 있고, 담당자는 서비스 메서드 안을 채웁니다. [← 처음으로](../README.md)

* 테이블 정의: [docs/ERD.md](../docs/ERD.md)
* 기능 ID → 화면 → API → 패키지 → 테이블: [docs/TRACEABILITY.md](../docs/TRACEABILITY.md)
* 화면 흐름: [docs/FLOWCHART.md](../docs/FLOWCHART.md)
* 표시: `★` 뼈대에서 동작하게 만들어 둔 공통 파일, `[이름]` 패키지 담당, 오른쪽 끝은 기능 ID / 테이블

## 👤 패키지 담당

| 담당 | 패키지 | 맡는 화면 |
|---|---|---|
| 유다연 | `auth` | 초기 메인(VIEW-01), 회원가입·로그인·비밀번호 재설정(VIEW-02~04) |
| 이은아 | `option` · `university` · `job` · `recommendation` · `qualification` · `examschedule` | 자격증 추천(VIEW-05), 자격증 상세(VIEW-06) |
| 이은아 | `global` · `CertSniperApplication` · `resources` | 모든 화면 (공통 설정·보안·오류 응답) |
| 박준성 | `targetexam` · `planner` · `memo` · `aiplanner` | 플래너 다이어리(VIEW-07), AI 플래너(VIEW-08) |
| 최기완 | `user` · `favorite` · `passstory` · `report` · `rank` | 저격왕(VIEW-09·10), 마이페이지(VIEW-11) |
| 황정규 | `support` · `admin` | 고객센터(VIEW-12·13), 관리자(VIEW-14~19) |

> `global`을 고쳐야 하면 이슈로 요청합니다. 담당이 바뀌면 이 표와 [CODEOWNERS](../.github/CODEOWNERS)를 같이 고칩니다.

## 📁 파일 트리

### 1. 프로젝트 설정

```
📁 backend/
├─ 📄 README.md                         (있음) 패키지 구조·이름 규칙
├─ 📄 build.gradle                    ★ Spring Boot 3.5.16 · Java 21. 의존성: web, data-jpa, security, validation, mail, mysql, jjwt, springdoc, jsoup, lombok
├─ 📄 settings.gradle                 ★ rootProject.name = 'cert-sniper'
├─ 📄 gradlew · gradlew.bat           ★ Gradle 래퍼 (Spring Initializr가 만들어 줌)
├─ 📁 gradle/wrapper/                 ★ gradle-wrapper.jar · gradle-wrapper.properties
└─ 📁 src/
   ├─ 📁 main/
   │  ├─ 📁 java/com/certsniper/        → 2 ~ 4번
   │  └─ 📁 resources/
   │     └─ 📄 application.yml        ★ 공통 설정. 비밀값은 레포 루트 `.env`에서 읽음
   └─ 📁 test/java/com/certsniper/      → 5번
```

### 2. 앱 시작 + global 공통

```
📁 java/com/certsniper/
├─ 📄 CertSniperApplication.java      ★ 앱 시작 (@SpringBootApplication, @EnableScheduling)
└─ 📁 global/                       [이은아]
   ├─ 📁 config/
   │  ├─ 📄 SecurityConfig.java       ★ URL별 권한 (GUEST·USER·ADMIN), JWT 필터 등록          CF-00-03
   │  ├─ 📄 WebConfig.java            ★ CORS (프론트 주소 허용)
   │  ├─ 📄 SwaggerConfig.java        ★ Swagger 문서 (/swagger-ui.html), JWT 인증 버튼
   │  ├─ 📄 JpaConfig.java            ★ 생성·수정 시각 자동 기록 (@EnableJpaAuditing)
   │  └─ 📄 AiClientConfig.java       ★ Qwen2.5(Ollama) 호출 RestClient, 읽기 타임아웃 60초      CF-06-03
   ├─ 📁 security/
   │  ├─ 📄 JwtTokenProvider.java     ★ Access·Refresh 토큰 발급·검증                           AUTH-02
   │  ├─ 📄 JwtAuthenticationFilter.java ★ 요청마다 토큰 확인, 정지 계정 차단                  AUTH-02, ADMIN-07
   │  ├─ 📄 LoginUser.java            ★ @LoginUser Long userId (컨트롤러에서 로그인 회원 받기)
   │  ├─ 📄 LoginUserArgumentResolver.java ★ @LoginUser 값 채우기
   │  └─ 📄 SecurityErrorHandler.java ★ 401(미인증)·403(권한 없음) JSON 응답                    CF-00-03
   ├─ 📁 exception/
   │  ├─ 📄 ErrorCode.java            ★ VALIDATION_ERROR · NOT_FOUND · DUPLICATE · LIMIT_EXCEEDED · FORBIDDEN …
   │  ├─ 📄 BusinessException.java    ★ 서비스에서 던지는 예외 (ErrorCode 포함)
   │  ├─ 📄 ErrorResponse.java        ★ 공통 오류 본문 {code, message, timestamp}
   │  └─ 📄 GlobalExceptionHandler.java ★ 예외 → 400·401·403·404·409·500, 구현 전 501
   ├─ 📁 common/
   │  ├─ 📄 HealthController.java     ★ GET /api/v1/health 서버 상태 확인
   │  ├─ 📄 BaseTimeEntity.java       ★ created_at (가입·작성·등록 일시가 있는 엔티티가 상속, updated_at 없음)
   │  └─ 📄 PageResponse.java         ★ 목록 응답 {items, total, page, size}
   └─ 📁 util/
      ├─ 📄 DateUtils.java            ★ 오늘 날짜(Asia/Seoul), LocalDate 변환                   CF-03-04
      └─ 📄 HtmlSanitizer.java        ★ 메모·게시글 허용 태그만 남기기 (XSS 방지)               CF-04-17, CF-05-06
```

### 3. 기능 패키지 (회원 · 추천 · 자격증)

```
📁 auth/  [유다연]                         회원가입·로그인·이메일 인증·비밀번호            AUTH-01·02·05·06
├─ 📁 controller/
│  └─ 📄 AuthController.java              /api/v1/auth/** (register, nickname-check, email-code, email-verify, login, token-refresh, logout, password-reset, password)
├─ 📁 service/
│  ├─ 📄 AuthService.java                 가입·로그인·로그아웃·토큰 재발급
│  ├─ 📄 EmailVerificationService.java    6자리 코드 발급·검증 (3분, 5회, 1일 제한)
│  ├─ 📄 EmailCodeStore.java              인증코드·인증 완료·실패 제한 메모리 저장 (ConcurrentHashMap, 키 = 용도:이메일, DB 저장 없음)
│  ├─ 📄 MailService.java                 인증 메일 발송
│  └─ 📄 PasswordService.java             비밀번호 재설정(로그인 전)·변경(마이페이지)
├─ 📁 repository/
│  └─ 📄 UserTokenRepository.java
├─ 📁 entity/
│  └─ 📄 UserToken.java                   user_tokens (로그인 Refresh Token)
└─ 📁 dto/
   ├─ 📄 RegisterRequest.java
   ├─ 📄 LoginRequest.java
   ├─ 📄 LoginResponse.java
   ├─ 📄 TokenRefreshRequest.java
   ├─ 📄 EmailCodeRequest.java
   ├─ 📄 EmailVerifyRequest.java
   ├─ 📄 PasswordResetRequest.java
   ├─ 📄 PasswordChangeRequest.java
   └─ 📄 NicknameCheckResponse.java

📁 user/  [최기완]                         회원정보 조회·변경, 탈퇴                           AUTH-03·04
├─ 📁 controller/
│  └─ 📄 UserController.java              /api/v1/users/me (조회·수정·탈퇴 신청·철회)
├─ 📁 service/
│  ├─ 📄 UserService.java                 회원정보 조회·수정·재검증
│  └─ 📄 WithdrawalService.java           탈퇴 대기·철회
├─ 📁 scheduler/
│  └─ 📄 WithdrawalCleanupScheduler.java  7일 유예 끝난 계정 삭제·비식별 처리 (매일 1회)
├─ 📁 repository/
│  ├─ 📄 UserRepository.java
│  ├─ 📄 UserCertificateRepository.java
│  └─ 📄 AccountStatusHistoryRepository.java
├─ 📁 entity/
│  ├─ 📄 User.java                        users
│  ├─ 📄 UserCertificate.java             user_certificates (보유 자격증)
│  └─ 📄 AccountStatusHistory.java        account_status_histories (탈퇴·정지 이력)
└─ 📁 dto/
   ├─ 📄 UserProfileResponse.java
   ├─ 📄 UserUpdateRequest.java
   └─ 📄 WithdrawRequest.java

📁 option/  [이은아]                       드롭다운·검색 목록 (엔티티 없음)                CF-01-01·02·07, CF-02-09
├─ 📁 controller/
│  └─ 📄 OptionController.java            GET /api/v1/options/{kind} (universities, majors, job_categories, interest_fields, qualifications, grades)
├─ 📁 service/
│  └─ 📄 OptionService.java               university·job·qualification 리포지토리 조회, 부분 일치·가나다순·페이지
└─ 📁 dto/
   ├─ 📄 OptionItem.java
   ├─ 📄 OptionResponse.java
   └─ 📄 SchoolInfoResponse.java

📁 university/  [이은아]                   대학·학부·전공 (조회 전용, ETL 적재)           CF-01-01·07
├─ 📁 repository/
│  ├─ 📄 UniversityRepository.java
│  ├─ 📄 CollegeRepository.java
│  └─ 📄 MajorRepository.java
└─ 📁 entity/
   ├─ 📄 University.java                  universities (학교 유형 = 학위 과정)
   ├─ 📄 College.java                     colleges
   └─ 📄 Major.java                       majors

📁 job/  [이은아]                          희망 직무·관심 분야 (조회 전용, ETL 적재)       CF-01-02
├─ 📁 repository/
│  ├─ 📄 JobCategoryRepository.java
│  └─ 📄 InterestFieldRepository.java
└─ 📁 entity/
   ├─ 📄 JobCategory.java                 job_categories (중직무분야 50, PK = Q-Net 코드)
   └─ 📄 InterestField.java               interest_fields (대직무분야 23, PK = Q-Net 코드)

📁 recommendation/  [이은아]               자격증 추천 (전공 맞춤 · 조건 선택)            CF-01-03~12
├─ 📁 controller/
│  └─ 📄 RecommendationController.java    POST /api/v1/recommendations(전공 맞춤) · /recommendations/conditions(조건 선택)
├─ 📁 service/
│  ├─ 📄 RecommendationService.java       전공 맞춤 추천(대학교·전공·학년)·최대 5개, 0개면 needs_school_info로 안내 구분  CF-01-05
│  ├─ 📄 ConditionRecommendationService.java 조건 선택 추천(학교·전공·학년 제외)        CF-01-09
│  ├─ 📄 CandidateMatcher.java            후보 만들기 (전공 맞춤: 학과 연결 / 조건 선택: 직무·관심, 보유 자격 하위 제외)  CF-01-03
│  ├─ 📄 EligibilityCalculator.java       응시 시점 판정·학년 시작 등급 (etl eligibility.py 이식)  CF-01-04
│  ├─ 📄 ScoreCalculator.java             점수표(상수) 합산·정렬                    CF-01-04
│  └─ 📄 RecommendationCardAssembler.java 카드 정보 (한 줄 소개·최근 합격률·다음 접수·시험일)  CF-01-10
├─ 📁 repository/
│  └─ 📄 MajorQualificationLinkRepository.java
├─ 📁 entity/
│  └─ 📄 MajorQualificationLink.java      major_qualification_links
└─ 📁 dto/
   ├─ 📄 RecommendationRequest.java
   ├─ 📄 ConditionRecommendationRequest.java 자격 구분·관심 분야·희망 직무
   ├─ 📄 RecommendationResponse.java      items 최대 5개(후보가 적으면 그 수만큼)
   ├─ 📄 RecommendationItem.java
   ├─ 📄 ScoreBreakdown.java              {factor, points, reason}                  CF-01-08
   └─ 📄 CardInfo.java

📁 qualification/  [이은아]                자격증 상세·합격률·트렌드                      CF-02-01~06·08·09, CF-00-02
├─ 📁 controller/
│  ├─ 📄 QualificationController.java     GET /api/v1/qualifications/{jmcd}
│  └─ 📄 TrendController.java             GET /api/v1/trends
├─ 📁 service/
│  ├─ 📄 QualificationService.java        기본정보·시험과목·응시자격·합격률
│  └─ 📄 TrendService.java                최근 완결 연도 1차 응시자 순위·5년 증감
├─ 📁 repository/
│  ├─ 📄 QualificationRepository.java
│  ├─ 📄 QualificationStatisticsYearlyRepository.java
│  └─ 📄 QualificationEligibilityRuleRepository.java
├─ 📁 entity/
│  ├─ 📄 Qualification.java               qualifications (자격구분 T/S, 등급 grade_level, 직무분야는 코드만)
│  ├─ 📄 QualificationStatisticsYearly.java qualification_statistics_yearly (접수자·응시자·합격자)
│  └─ 📄 QualificationEligibilityRule.java qualification_eligibility_rules (응시 조건 목록)
└─ 📁 dto/
   ├─ 📄 QualificationDetailResponse.java
   ├─ 📄 EligibilityResponse.java         응시 조건 필요/없음 + 조건 목록          CF-02-03
   ├─ 📄 PassRateResponse.java
   └─ 📄 TrendResponse.java

📁 examschedule/  [이은아]                 시험일정·회차 상태·D-Day                        CF-02-10, CF-03-04
├─ 📁 controller/
│  └─ 📄 ExamScheduleController.java      GET /api/v1/exam-schedules/{id}/dday
├─ 📁 service/
│  ├─ 📄 ExamScheduleService.java         회차 상태(정기·빈자리 접수 포함)·다음 회차
│  └─ 📄 DdayCalculator.java              D-n · D-Day · 종료 (planner·targetexam도 사용)
├─ 📁 repository/
│  └─ 📄 QualificationExamScheduleRepository.java
├─ 📁 entity/
│  └─ 📄 QualificationExamSchedule.java   qualification_exam_schedules (정기·빈자리 접수)
└─ 📁 dto/
   ├─ 📄 ExamScheduleResponse.java
   └─ 📄 DdayResponse.java
```

### 4. 기능 패키지 (개인화 · 저격왕 · 고객센터 · 관리자)

```
📁 favorite/  [최기완]                     찜·관심 자격증                                  CF-03-01·02·09
├─ 📁 controller/  📄 FavoriteController.java       /api/v1/favorites
├─ 📁 service/     📄 FavoriteService.java
├─ 📁 repository/  📄 FavoriteRepository.java
├─ 📁 entity/      📄 Favorite.java                 favorites
└─ 📁 dto/         📄 FavoriteResponse.java

📁 targetexam/  [박준성]                   목표 자격증·목표 시험일                         CF-03-03, CF-04-04~06·09~11
├─ 📁 controller/  📄 TargetExamController.java     /api/v1/target-exams
├─ 📁 service/     📄 TargetExamService.java        추가·수정(시험일·색상 중복 금지)·삭제(일정·메모 같이)
├─ 📁 repository/  📄 TargetExamRepository.java
├─ 📁 entity/      📄 TargetExam.java               target_exams
└─ 📁 dto/
   ├─ 📄 TargetExamRequest.java
   └─ 📄 TargetExamResponse.java

📁 planner/  [박준성]                      플래너 다이어리                                 CF-04-03·07·12~16·18·19, CF-00-04
├─ 📁 controller/
│  ├─ 📄 PlannerController.java           /api/v1/planner (모드·일정바·당일 일정·개인 일정)
│  ├─ 📄 TodayScheduleController.java     GET /api/v1/planner/today (메인 오늘의 일정)
│  └─ 📄 DailyIdiomController.java        GET /api/v1/daily-idiom (오늘의 사자성어, 로그인 무관)  CF-00-04, CF-04-19
├─ 📁 service/
│  ├─ 📄 PlannerService.java              타임라인 조회·일정 등록·수정·삭제·모드 전환
│  ├─ 📄 ScheduleLimitValidator.java      6개 제한 (aiplanner도 사용)              CF-04-16
│  └─ 📄 DailyIdiomService.java           오늘의 사자성어 (기준일부터 1~50번 하루 1개씩 순환, etl daily_idiom 이식)  CF-00-04, CF-04-19
├─ 📁 repository/
│  ├─ 📄 PlannerRepository.java
│  ├─ 📄 PersonalScheduleRepository.java
│  ├─ 📄 PlannerBarRepository.java
│  ├─ 📄 PlannerMilestoneRepository.java
│  └─ 📄 DailyIdiomRepository.java
├─ 📁 entity/
│  ├─ 📄 Planner.java                     planners (모드·6개 제한 잠금)
│  ├─ 📄 PersonalSchedule.java            personal_schedules
│  ├─ 📄 PlannerBar.java                  planner_bars (일정바·당일 일정)
│  ├─ 📄 PlannerMilestone.java            planner_milestones (세부 일정·AI 일일 항목·휴식)
│  └─ 📄 DailyIdiom.java                  daily_idioms (사자성어 50개, display_order 순서로 순환)
└─ 📁 dto/
   ├─ 📄 PlannerResponse.java
   ├─ 📄 BarRequest.java
   ├─ 📄 MilestoneRequest.java
   ├─ 📄 PersonalScheduleRequest.java
   ├─ 📄 TodayScheduleResponse.java
   └─ 📄 DailyIdiomResponse.java

📁 memo/  [박준성]                         자격증별 메모                                   CF-04-01·08·17
├─ 📁 controller/  📄 MemoController.java           /api/v1/target-exams/{id}/memo
├─ 📁 service/     📄 MemoService.java              500자·서식 검증
├─ 📁 repository/  📄 MemoRepository.java
├─ 📁 entity/      📄 Memo.java                     memos (목표당 1개)
└─ 📁 dto/
   ├─ 📄 MemoRequest.java
   └─ 📄 MemoResponse.java

📁 aiplanner/  [박준성]                    AI 플래너 (엔티티 없음)                         CF-06-01~05
├─ 📁 controller/  📄 AiPlannerController.java      POST /api/v1/ai-planner
├─ 📁 service/
│  ├─ 📄 AiPlannerService.java            진입 검사·저장·기존 일정 교체
│  ├─ 📄 QwenClient.java                  Qwen2.5 API 호출·프롬프트
│  └─ 📄 AiPlanValidator.java             JSON·날짜·기간·연속성·참조 검증
└─ 📁 dto/
   ├─ 📄 SurveyRequest.java               7단계 설문
   └─ 📄 AiPlanResponse.java              meta · timelineBars · dailyPoints

📁 passstory/  [최기완]                    저격왕 합격수기                                 CF-05-01~08·10·11
├─ 📁 controller/  📄 PassStoryController.java      /api/v1/pass-stories (?jmcds= 추천 페이지 미리보기 포함)
├─ 📁 service/     📄 PassStoryService.java         목록·검색·상세·등록·수정(재검토 대기)·Soft Delete
├─ 📁 repository/  📄 PassStoryRepository.java
├─ 📁 entity/      📄 PassStory.java                   pass_stories
└─ 📁 dto/
   ├─ 📄 PassStoryRequest.java
   ├─ 📄 PassStoryCardResponse.java
   └─ 📄 PassStoryDetailResponse.java

📁 report/  [최기완]                       신고·즉시 숨김·내 신고내역                      CF-05-09·13
├─ 📁 controller/  📄 ReportController.java         /api/v1/pass-stories/{id}/reports, /api/v1/users/me/reports
├─ 📁 service/     📄 ReportService.java
├─ 📁 repository/
│  ├─ 📄 PassStoryReportRepository.java
│  └─ 📄 PassStoryModerationLogRepository.java
├─ 📁 entity/
│  ├─ 📄 PassStoryReport.java             pass_story_reports
│  └─ 📄 PassStoryModerationLog.java      pass_story_moderation_logs
└─ 📁 dto/
   ├─ 📄 ReportRequest.java
   └─ 📄 MyReportResponse.java

📁 rank/  [최기완]                         저격 등급·승급 알림                              CF-05-04·12
├─ 📁 controller/  📄 RankController.java           GET /api/v1/ranks/me
├─ 📁 service/     📄 RankService.java              등급 자동 계산 (passstory·admin도 사용), 승급이면 등록 응답에 이전·새 등급
├─ 📁 repository/  📄 UserRankRepository.java
├─ 📁 entity/      📄 UserRank.java                    user_ranks
└─ 📁 dto/
   ├─ 📄 RankResponse.java
   └─ 📄 RankUpResponse.java

📁 support/  [황정규]                      고객센터                                         CS-01~05
├─ 📁 controller/
│  ├─ 📄 NoticeController.java            /api/v1/notices
│  ├─ 📄 FaqController.java               /api/v1/faqs
│  └─ 📄 InquiryController.java           /api/v1/inquiries (등록·나의 문의 내역)
├─ 📁 service/
│  ├─ 📄 NoticeService.java
│  ├─ 📄 FaqService.java
│  └─ 📄 InquiryService.java
├─ 📁 repository/
│  ├─ 📄 NoticeRepository.java
│  ├─ 📄 FaqRepository.java
│  ├─ 📄 InquiryRepository.java
│  └─ 📄 InquiryAnswerRepository.java
├─ 📁 entity/
│  ├─ 📄 Notice.java                      notices
│  ├─ 📄 Faq.java                         faqs
│  ├─ 📄 Inquiry.java                     inquiries
│  └─ 📄 InquiryAnswer.java               inquiry_answers
├─ 📁 enums/       📄 InquiryType.java              문의 유형 5개(표시명 포함, DB CHECK와 같은 값)   CS-04
└─ 📁 dto/
   ├─ 📄 NoticeResponse.java
   ├─ 📄 FaqResponse.java
   ├─ 📄 InquiryRequest.java
   └─ 📄 InquiryResponse.java

📁 admin/  [황정규]                        관리자 (/api/v1/admin/**, ADMIN 재확인)         ADMIN-02~11
├─ 📁 controller/
│  ├─ 📄 AdminDashboardController.java    대시보드 건수                             ADMIN-10
│  ├─ 📄 AdminUserController.java         회원 목록·상세·정정·정지  ADMIN-02·07
│  ├─ 📄 AdminNoticeController.java       공지 등록·수정·삭제                       ADMIN-03
│  ├─ 📄 AdminInquiryController.java      문의 답변                                 ADMIN-04
│  ├─ 📄 AdminReportController.java       신고 검토·재검토                          ADMIN-05·06
│  └─ 📄 AdminPassStoryController.java    저격왕 전체 글                            ADMIN-11
├─ 📁 service/
│  ├─ 📄 AdminDashboardService.java
│  ├─ 📄 AdminUserService.java
│  ├─ 📄 AdminReportService.java
│  └─ 📄 AuditLogService.java             변경 전·후·관리자·시각 기록
├─ 📁 repository/  📄 AdminAuditLogRepository.java
├─ 📁 entity/      📄 AdminAuditLog.java            admin_audit_logs
└─ 📁 dto/
   ├─ 📄 DashboardResponse.java
   ├─ 📄 AdminUserDetailResponse.java
   ├─ 📄 ProfileCorrectionRequest.java
   ├─ 📄 SuspendRequest.java
   ├─ 📄 ReviewRequest.java
   ├─ 📄 InquiryAnswerRequest.java
   ├─ 📄 NoticeRequest.java
   └─ 📄 AdminReportResponse.java
```

### 5. 테스트

기능을 구현하면서 담당자가 추가합니다. 아래는 정해 둔 이름입니다.

```
📁 test/java/com/certsniper/
├─ 📄 CertSniperApplicationTests.java      앱이 뜨는지
├─ 📁 recommendation/
│  ├─ 📄 RecommendationGoldenTest.java     etl/tests/golden_recommendations.json과 결과 비교
│  ├─ 📄 EligibilityCalculatorTest.java    학교 종류·학년·등급별 응시 시점·시작 등급
│  └─ 📄 ScoreCalculatorTest.java
├─ 📁 qualification/
│  ├─ 📄 QualificationServiceTest.java     합격률 계산(응시자 0 → 빈값)
│  └─ 📄 TrendServiceTest.java
├─ 📁 examschedule/
│  └─ 📄 DdayCalculatorTest.java           D-n · D-Day · 종료, 회차 상태
└─ 📁 (각 패키지)/                          담당자가 같은 구조로 추가 (예: planner/ScheduleLimitValidatorTest.java)
```

> 엔티티 31개 = auth 1 · user 3 · university 3 · job 2 · recommendation 1 · qualification 3 · examschedule 1 · favorite 1 · targetexam 1 · planner 5 · memo 1 · passstory 1 · report 2 · rank 1 · support 4 · admin 1

## 🧱 global 공통을 쓰는 곳

| 공통 | 쓰는 패키지 |
|---|---|
| `@LoginUser`, `SecurityConfig` | 회원 전용 API 전부 (CF-00-03) |
| `ErrorResponse`, `GlobalExceptionHandler` | 전부 (오류 응답 `code`·`message`·`timestamp`) |
| `BaseTimeEntity` | user, favorite, targetexam, planner, passstory, report, support, admin (created_at이 있는 테이블) |
| `PageResponse` | option, passstory, report, support, admin |
| `DateUtils` | examschedule(D-Day·회차 상태), planner(오늘의 일정), recommendation(남은 시험) |
| `HtmlSanitizer` | memo, passstory |
| `spring.mail` (application.yml, JavaMailSender 자동 설정) | auth |
| `AiClientConfig` | aiplanner |

## 🔗 패키지끼리 부르는 곳

다른 패키지는 **아래 메서드만 호출**합니다. 구현은 그 패키지 담당이 하고, 더 필요하면 이슈로 요청합니다.

| 부르는 쪽 | 부르는 메서드 (담당) | 이유 | 기능 ID |
|---|---|---|---|
| auth | `UserService.createUser` · `findByEmail` · `updatePassword` · `isNicknameAvailable` (최기완) | 가입 저장, 로그인·재설정 회원 조회, 비밀번호 저장, 닉네임 중복 | AUTH-01·02·05·06 |
| auth | `EmailVerificationService.checkVerified` (유다연) | 가입·재설정·변경 전 이메일 인증 완료 확인 | AUTH-01·05·06 |
| auth, user | `OptionService.validateSchoolInfo` (이은아) | 대학·학부·전공·학년 묶음 검증 | CF-01-01 |
| global/security | `UserRepository` (최기완) | 정지·탈퇴 계정 차단 (필터에서 직접 조회) | AUTH-02, ADMIN-07 |
| user | `RankService.getCurrentRank` (최기완) | 회원정보의 저격 등급 | AUTH-03 |
| recommendation | `UserService.getRecommendationProfile` (최기완) | 전공 맞춤 추천에 회원 학력·보유 자격 | CF-01-03 |
| recommendation | `QualificationService` · `ExamScheduleService.getSchedules` (이은아) | 카드의 한 줄 소개·최근 합격률·다음 접수·가까운 시험일 | CF-01-10 |
| recommendation | `FavoriteService.getFavoriteJmcds` (최기완) | 카드의 찜 상태 | CF-03-01·02 |
| qualification | `ExamScheduleService.getSchedules` (이은아) | 상세의 시험일정 | CF-02-10 |
| favorite, passstory, aiplanner | `QualificationService.getInfo` (이은아) | 자격증 이름·등급·한 줄 소개·시험과목 | CF-03-09, CF-05-06, CF-06-02 |
| targetexam, planner, aiplanner | `ExamScheduleService.getSchedules`, `DdayCalculator.daysRemaining` · `label` (이은아) | 시험일 회차 선택·D-Day | CF-03-03·04, CF-04-05·06, CF-06-02 |
| targetexam | `OptionService.getOptions("qualifications", …)` (이은아) | 목표 자격증 종목명 검색 | CF-04-09 |
| targetexam, planner, aiplanner | `ScheduleLimitValidator.validateCanAdd` (박준성) | 일정 6개 제한 | CF-04-16, CF-06-01 |
| aiplanner | `TargetExamService.getOrCreateTargetExam`, `PlannerService.replaceAiSchedule` (박준성) | 플랜 저장, 기존 AI 일정 교체 | CF-06-05 |
| auth | `MailService.sendVerificationCode` (유다연) | 인증 메일 발송 | AUTH-01·05·06 |
| passstory, admin | `RankService.recalculate` (최기완) | 등록·삭제·숨김 확정·재검토 후 등급 계산·승급 알림 | CF-05-06·12 |
| report | `PassStoryService.hideByReport` (최기완) | 신고 즉시 숨김 | CF-05-09 |
| admin | `PassStoryService.changeStatus`, `UserService.getUser` (최기완) | 신고 검토·재검토, 회원 상세 | ADMIN-02·05·06·07 |
| admin | `UserService.countUsers`, `ReportService.countPending` (최기완), `NoticeService.countNotices`, `InquiryService.countUnanswered` (황정규), `QualificationService.countQualifications` (이은아) | 대시보드 건수 | ADMIN-10 |
| admin | `AuditLogService.record` (황정규) | 관리자 변경 이력 | ADMIN-02~07 |
| auth 로그인 | `UserService.releaseExpiredSuspension` (최기완) | 정지 기간이 끝난 계정 되돌리기 | AUTH-02, ADMIN-07 |
| user 탈퇴·비밀번호 변경, admin 정지 | `AuthService.revokeAllTokens` (유다연) | 로그인 토큰 전체 폐기 | AUTH-04·05·06, ADMIN-07 |
| user 탈퇴 확정 | `FavoriteService.deleteAllByUser` (최기완), `TargetExamService.deleteAllByUser` · `PlannerService.deleteAllByUser` (박준성) | 개인 데이터 삭제 (회원 행은 비식별 처리, 글·문의는 남음) | AUTH-04 |
| user 회원정보 | `OptionService.getSchoolNames` (이은아) | 대학·학부·학과 표시명 | AUTH-03 |
| planner, memo | `TargetExamService.validateOwner` (박준성) | 내 목표 자격증인지 확인 | CF-04-01·07·14 |
| planner | `TargetExamService.countByUser` (박준성) | 일정 6개 제한 계산 | CF-04-16 |
| passstory, support, admin 목록 | `UserService.getNicknames` (최기완), `RankService.getCurrentRanks` (최기완) | 작성자 닉네임·등급 한 번에 조회 | CF-05-01·05, ADMIN-02·04·11 |
| passstory, admin | `ReportService.recordModeration` (최기완) | 수정·삭제·숨김·재검토 상태 변경 이력 | CF-05-07·08·10, ADMIN-05·06 |
| admin | `ReportService.getReportsForAdmin` · `resolve` (최기완), `UserService.addConfirmedHidden` · `searchUsers` · `correctProfile` · `suspend` · `unsuspend` (최기완) | 신고 목록·처리, 누적 숨김 횟수 증가(정지는 관리자가 직접), 회원 관리 | ADMIN-02·05·07 |

## 이름 규칙

| 대상 | 규칙 | 예시 |
|---|---|---|
| 패키지 | 소문자 한 단어 | `recommendation`, `passstory` |
| 클래스 | 대문자로 시작, 역할을 뒤에 붙임 | `PassStoryController`, `PassStoryService` |
| 엔티티 | 테이블 이름의 단수형 | `pass_stories` → `PassStory` |
| DTO | 요청 `~Request`, 응답 `~Response` | `RegisterRequest`, `RecommendationResponse` |
| API 경로 | `/api/v1/` + 복수형 소문자, 단어 사이 `-` | `/api/v1/pass-stories`, `/api/v1/exam-schedules` |

## 뼈대 사용법

1. DB는 팀 공용 DB를 씁니다. 따로 설치·실행하지 않습니다
2. 백엔드 설정은 레포 루트 `.env`를 그대로 읽습니다. 따로 만들 설정 파일은 없습니다

   | `.env` 칸 | 넣을 값 |
   |---|---|
   | `DB_HOST` · `DB_PORT` · `DB_NAME` · `DB_USERNAME` · `DB_PASSWORD` | 공용 DB 접속 정보 (카톡으로 받음). 비어 있으면 백엔드가 실행되지 않습니다 |
   | `JWT_SECRET` | 카톡으로 받음 |
   | `MAIL_USERNAME` · `MAIL_PASSWORD` | 팀 프로젝트용 Gmail 계정·앱 비밀번호 (카톡으로 받음). 비워 두면 인증 메일만 안 가고 나머지는 실행됩니다 |
3. `./gradlew bootRun` (Windows `gradlew.bat bootRun`) → http://localhost:8080/swagger-ui.html 에서 전체 API 확인
4. 각자 `feature/{파트}/{작업}` 브랜치에서 자기 서비스 메서드의 `throw new BusinessException(ErrorCode.NOT_IMPLEMENTED)`를 지우고 구현합니다. 구현 전 API는 501 `NOT_IMPLEMENTED`로 응답합니다

| 정해진 것 | 내용 |
|---|---|
| 엔티티 | 테이블과 1:1, 컬럼 이름·타입 그대로 (`ddl-auto: validate`). 다른 테이블은 객체 대신 ID(`Long userId`)로 가집니다. 생성자·변경 메서드는 담당자가 엔티티 안에 추가합니다 |
| 로그인 회원 | 컨트롤러 파라미터 `@LoginUser Long userId` (비로그인이면 401). 정지 계정은 필터에서 403 `ACCOUNT_SUSPENDED` |
| JSON 이름 | 요청·응답 모두 snake_case (`spring.jackson.property-naming-strategy`). DTO는 camelCase로 쓰면 자동 변환됩니다 |
| 목록 응답 | `PageResponse.of(page)` → `{items, total, page, size}` |
| 오류 | `throw new BusinessException(ErrorCode.XXX)` → `{code, message, timestamp}`. 코드가 더 필요하면 `ErrorCode`에 추가하고 PR에 적습니다 |
| 날짜 | 오늘·지금은 `DateUtils.today()`·`DateUtils.now()` (Asia/Seoul) |
| 상태 코드 | 조회·수정 결과 반환 200, 등록 201, 반환값 없는 수정·삭제 204 |

## 지켜 주세요

* 다른 기능 패키지의 클래스를 고치지 않습니다. 여러 기능이 같이 쓰는 것은 `global/`에 둡니다
* DB 구조는 DB 관리자(이은아)만 공용 DB에 반영합니다. 엔티티는 테이블에 맞추고(`ddl-auto: validate`), 컬럼이 필요하면 이슈로 요청합니다
* DB 비밀번호·JWT 키·SMTP 앱 비밀번호·AI API 키는 루트 `.env`에 둡니다 (커밋 금지)
* 추천·자격증 로직(`recommendation`, `qualification`, `examschedule`)의 기준 구현은 [etl/](../etl/README.md)에 있습니다. 같은 입력에 같은 결과가 나와야 합니다
