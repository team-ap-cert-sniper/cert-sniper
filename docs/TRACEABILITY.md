# 🔎 기능 추적표

기능 ID에서 담당 → 화면 → 프론트 위치 → API → 백엔드 패키지 → 테이블까지 따라가는 표입니다. 기준: 기능 명세서 v1.4.0 · 요구사항 정의서 v1.8.0 · DB 설계서 Ver.7. [← 처음으로](../README.md)

* API가 `담당 파트 설계`인 칸은 그 파트가 정해서 채웁니다. 추천·자격증 API는 [etl/docs/API_CONTRACT.md](../etl/docs/API_CONTRACT.md)에 정해져 있습니다
* 담당은 화면 기준 임시 배정입니다. 다른 담당 패키지를 부르는 곳은 [backend/README.md](../backend/README.md) "패키지끼리 부르는 곳"에 있습니다
* 테이블이 `(저장 없음)`이면 화면 동작이나 계산만 있는 기능입니다

| 기능 ID | 기능명 | 담당 | 화면 | 프론트 위치 (`frontend/src/`) | API | 백엔드 패키지 | 테이블 |
|---|---|---|---|---|---|---|---|
| AUTH-01 | 회원가입 및 온보딩 | 유다연 | VIEW-02 | pages/Auth | 담당 파트 설계 | auth, user | users, email_verifications, email_auth_limits, user_certificates |
| AUTH-02 | 로그인 / 로그아웃 | 유다연 | VIEW-03 | pages/Auth | 담당 파트 설계 | auth | users, user_tokens |
| AUTH-03 | 회원정보 조회·변경 | 최기완 | VIEW-11 | pages/MyPage | 담당 파트 설계 | user | users, user_certificates |
| AUTH-04 | 회원 탈퇴 | 최기완 | VIEW-11 | pages/MyPage | 담당 파트 설계 | user | users, account_status_histories |
| AUTH-05 | 비밀번호 재설정 | 유다연 | VIEW-04 | pages/Auth | 담당 파트 설계 | auth | email_verifications, email_auth_limits |
| AUTH-06 | 비밀번호 변경(마이페이지) | 최기완 | VIEW-11 | pages/MyPage | 담당 파트 설계 | auth | email_verifications, email_auth_limits |
| CF-00-01 | 공통 헤더 및 메뉴 이동 | 공통 | 공통 | components/common, router | 담당 파트 설계 | global/security | (저장 없음) |
| CF-00-02 | 초기 메인 화면 | 유다연 | VIEW-01 | pages/Main | GET /api/v1/trends | qualification | qualification_statistics_yearly |
| CF-00-03 | 비회원 회원 전용 기능 접근 | 공통 | 공통 | components/common, router | 담당 파트 설계 | global/security | (저장 없음) |
| CF-00-04 | 오늘의 일정 패널 | 유다연 | VIEW-01 | pages/Main | 담당 파트 설계 | planner | planner_bars |
| CF-01-01 | 대학교·전공 선택 | 이은아 | VIEW-02·05·11 | pages/Auth, pages/Recommend, pages/MyPage | GET /api/v1/options/schools · majors | option, university | universities, colleges, majors |
| CF-01-02 | 희망 직무·관심 분야 선택 (추천 페이지 조건 선택) | 이은아 | VIEW-05 | pages/Recommend | GET /api/v1/options/job_fields · interest_fields | option, job | job_categories, interest_fields |
| CF-01-03 | 추천 데이터 매칭 | 이은아 | VIEW-05 | pages/Recommend | POST /api/v1/recommendations | recommendation | user_certificates, major_qualification_links, qualifications |
| CF-01-04 | 추천 우선순위 산정 | 이은아 | VIEW-05 | pages/Recommend | POST /api/v1/recommendations | recommendation | recommendation_weight_policies (MAJOR) |
| CF-01-05 | 추천 결과 조회 | 이은아 | VIEW-05 | pages/Recommend | POST /api/v1/recommendations | recommendation | (저장 안 함) |
| CF-01-06 | 자격증 상세 이동 | 이은아 | VIEW-05·06 | pages/Recommend, pages/QualificationDetail | GET /api/v1/qualifications/{jmcd} | qualification | (저장 없음) |
| CF-01-07 | 대학교·전공 목록 조회 | 이은아 | VIEW-02·05·11 | pages/Auth, pages/Recommend, pages/MyPage | GET /api/v1/options/schools · majors | option, university | universities, colleges, majors, major_aliases |
| CF-01-08 | 추천 근거 표시 | 이은아 | VIEW-05 | pages/Recommend | POST /api/v1/recommendations | recommendation | major_qualification_links |
| CF-01-09 | 추천 조건 선택 및 반영 | 이은아 | VIEW-05 | pages/Recommend | POST /api/v1/recommendations/conditions | recommendation | interest_fields, job_categories, qualifications, recommendation_weight_policies (CONDITION) |
| CF-01-10 | 추천 자격증 카드 | 이은아 | VIEW-05 | pages/Recommend | POST /api/v1/recommendations | recommendation | qualifications, qualification_statistics_yearly, qualification_exam_schedules |
| CF-01-11 | 카드에서 AI 플래너 생성하기 | 이은아 · 박준성 | VIEW-05·08 | pages/Recommend, pages/AiPlanner | 담당 파트 설계 | aiplanner | (저장 없음) |
| CF-01-12 | 추천 페이지 트렌드·합격수기 섹션 | 이은아 | VIEW-05 | pages/Recommend | GET /api/v1/trends | qualification, passstory | qualification_statistics_yearly, pass_stories |
| CF-02-01 | 자격증 기본정보 조회 | 이은아 | VIEW-06 | pages/QualificationDetail | GET /api/v1/qualifications/{jmcd} | qualification | qualifications |
| CF-02-02 | 시험과목 조회 | 이은아 | VIEW-06 | pages/QualificationDetail | GET /api/v1/qualifications/{jmcd} | qualification | qualifications, qualification_subject_items |
| CF-02-03 | 응시자격 조회 | 이은아 | VIEW-06 | pages/QualificationDetail | GET /api/v1/qualifications/{jmcd} | qualification | qualifications, qualification_eligibility_rules |
| CF-02-04 | 합격률 조회 | 이은아 | VIEW-06 | pages/QualificationDetail | GET /api/v1/qualifications/{jmcd} | qualification | qualification_statistics_yearly |
| CF-02-05 | 합격률 그래프 | 이은아 | VIEW-06 | pages/QualificationDetail | GET /api/v1/qualifications/{jmcd} | qualification | qualification_statistics_yearly |
| CF-02-06 | 연도별 상세조회 | 이은아 | VIEW-06 | pages/QualificationDetail | GET /api/v1/qualifications/{jmcd} | qualification | qualification_statistics_yearly |
| CF-02-07 | 난이도 조회 | 이은아 | VIEW-06 | pages/QualificationDetail | GET /api/v1/qualifications/{jmcd} | qualification | (저장 없음) |
| CF-02-08 | 주목받는 자격증 트렌드 | 이은아 | VIEW-01·05·06 | pages/Main, pages/Recommend, pages/QualificationDetail | GET /api/v1/trends | qualification | qualification_statistics_yearly |
| CF-02-09 | 자격증 검색 및 상세 첫 화면 | 이은아 | VIEW-06 | pages/QualificationDetail | GET /api/v1/options/qualifications · interest_fields · job_fields | option, qualification | qualifications |
| CF-02-10 | 시험일정 조회 | 이은아 | VIEW-06 | pages/QualificationDetail | GET /api/v1/qualifications/{jmcd} | examschedule | qualification_exam_schedules |
| CF-03-01 | 찜 등록 | 이은아 | VIEW-05 | pages/Recommend | 담당 파트 설계 | favorite | favorites |
| CF-03-02 | 찜 해제 | 이은아 · 최기완 | VIEW-05·11 | pages/Recommend, pages/MyPage | 담당 파트 설계 | favorite | favorites |
| CF-03-03 | 목표 시험일 설정 | 박준성 | VIEW-07·08 | pages/Planner, pages/AiPlanner | 담당 파트 설계 | targetexam, examschedule | qualification_exam_schedules, target_exams |
| CF-03-04 | D-Day 계산 | 박준성 | VIEW-07 | pages/Planner | GET /api/v1/exam-schedules/{id}/dday | examschedule | target_exams |
| CF-03-09 | 관심 자격증 조회 및 AI 플래너 만들기 | 최기완 | VIEW-11 | pages/MyPage | 담당 파트 설계 | favorite | favorites |
| CF-04-01 | 자격증별 메모 | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | memo | memos |
| CF-04-03 | 작은 캘린더 날짜 선택 | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | planner | planner_bars |
| CF-04-04 | 목표 자격증 목록 | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | targetexam | target_exams |
| CF-04-05 | 시험 날짜 표시 | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | targetexam | target_exams |
| CF-04-06 | 시험 잔여기간(D-Day) | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | targetexam, examschedule | target_exams |
| CF-04-07 | 자격증 일정 수정·삭제 | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | planner | planner_bars |
| CF-04-08 | 메모 팝업 축소·확장 | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | memo | memos |
| CF-04-09 | 목표 자격증 추가 | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | targetexam | target_exams |
| CF-04-10 | 목표 자격증 수정 | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | targetexam | target_exams |
| CF-04-11 | 목표 자격증 삭제 | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | targetexam | target_exams |
| CF-04-12 | 플래너 모드 표시·전환 | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | planner | planners |
| CF-04-13 | AI 일정 렌더링 | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | planner | planner_bars, planner_milestones |
| CF-04-14 | 자격증 일정 등록·수정 팝업 | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | planner | planner_bars, planner_milestones |
| CF-04-15 | 당일 일정(마일스톤) | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | planner | planner_milestones |
| CF-04-16 | 일정 개수 제한 | 박준성 | VIEW-07·08 | pages/Planner, pages/AiPlanner | 담당 파트 설계 | planner | planners, personal_schedules |
| CF-04-17 | 메모 편집 팝업·서식 | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | memo | memos |
| CF-04-18 | 개인(일반) 일정 | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | planner | personal_schedules |
| CF-04-19 | 타임라인·사자성어 표시 | 박준성 | VIEW-07 | pages/Planner | 담당 파트 설계 | planner | (저장 없음) |
| CF-06-01 | AI 플래너 진입 | 박준성 | VIEW-08 | pages/AiPlanner | 담당 파트 설계 | aiplanner | planners |
| CF-06-02 | 7단계 설문 입력 | 박준성 | VIEW-08 | pages/AiPlanner | 담당 파트 설계 | aiplanner | qualification_exam_schedules |
| CF-06-03 | AI 플랜 생성 요청 | 박준성 | VIEW-08 | pages/AiPlanner | 담당 파트 설계 | aiplanner | qualification_subject_items |
| CF-06-04 | AI 결과 검증 | 박준성 | VIEW-08 | pages/AiPlanner | 담당 파트 설계 | aiplanner | (저장 없음) |
| CF-06-05 | 플랜 저장 및 다이어리 이동 | 박준성 | VIEW-08·07 | pages/AiPlanner, pages/Planner | 담당 파트 설계 | aiplanner | target_exams, planner_bars, planner_milestones |
| CF-05-01 | 합격수기 목록 조회 | 최기완 | VIEW-09 | pages/PassStory | 담당 파트 설계 | passstory | pass_stories |
| CF-05-02 | 합격수기 검색·정렬 | 최기완 | VIEW-09 | pages/PassStory | 담당 파트 설계 | passstory | pass_stories |
| CF-05-03 | 전체보기·내가 쓴 글 보기 | 최기완 | VIEW-09·11 | pages/PassStory, pages/MyPage | 담당 파트 설계 | passstory | pass_stories |
| CF-05-04 | 저격 등급 대시보드 | 최기완 | VIEW-09 | pages/PassStory | 담당 파트 설계 | rank | pass_stories, user_ranks |
| CF-05-05 | 카드·상세 조회 | 최기완 | VIEW-09·10 | pages/PassStory | 담당 파트 설계 | passstory | pass_stories |
| CF-05-06 | 합격수기 등록 | 최기완 | VIEW-10 | pages/PassStory | 담당 파트 설계 | passstory | qualification_subject_items, pass_stories, pass_story_subject_difficulties |
| CF-05-07 | 합격수기 수정 | 최기완 | VIEW-10 | pages/PassStory | 담당 파트 설계 | passstory | pass_stories |
| CF-05-08 | 합격수기 삭제 | 최기완 | VIEW-09·10 | pages/PassStory | 담당 파트 설계 | passstory | pass_stories |
| CF-05-09 | 합격수기 신고 | 최기완 | VIEW-10 | pages/PassStory | 담당 파트 설계 | report | pass_story_reports, pass_story_moderation_logs |
| CF-05-10 | 숨김 안내 및 수정 요청 | 최기완 | VIEW-09·11 | pages/PassStory, pages/MyPage | 담당 파트 설계 | passstory | pass_stories |
| CF-05-11 | 페이지네이션 | 최기완 | VIEW-09 | pages/PassStory | 담당 파트 설계 | passstory | (저장 없음) |
| CF-05-12 | 등급 승급 알림 팝업 | 최기완 | VIEW-09 | pages/PassStory | 담당 파트 설계 | rank | rank_notifications |
| CF-05-13 | 내 신고내역 | 최기완 | VIEW-11 | pages/MyPage | 담당 파트 설계 | report | pass_story_reports |
| ADMIN-02 | 회원 목록·상세·정보 정정 | 황정규 | VIEW-15 | pages/Admin | 담당 파트 설계 | admin, user | users, admin_audit_logs |
| ADMIN-03 | 공지사항 관리 | 황정규 | VIEW-16 | pages/Admin | 담당 파트 설계 | admin, support | notices |
| ADMIN-04 | 1:1 문의 관리 | 황정규 | VIEW-17 | pages/Admin | 담당 파트 설계 | admin, support | inquiries, inquiry_answers |
| ADMIN-05 | 신고 검토 및 숨김 확정·해제 | 황정규 | VIEW-18 | pages/Admin | 담당 파트 설계 | admin, report | pass_story_reports, pass_story_moderation_logs, admin_audit_logs |
| ADMIN-06 | 재검토·숨김 해제·삭제 | 황정규 | VIEW-18 | pages/Admin | 담당 파트 설계 | admin, report | pass_story_moderation_logs, admin_audit_logs |
| ADMIN-07 | 계정 정지·해제 | 황정규 | VIEW-15 | pages/Admin | 담당 파트 설계 | admin, user | users, account_status_histories, admin_audit_logs |
| ADMIN-08 | 등급 수동 보정 | 황정규 | VIEW-15 | pages/Admin | 담당 파트 설계 | admin, rank | user_ranks, user_rank_histories, admin_audit_logs |
| ADMIN-09 | 등급 재산정 | 황정규 | VIEW-15 | pages/Admin | 담당 파트 설계 | admin, rank | user_ranks, user_rank_histories, admin_audit_logs |
| ADMIN-10 | 관리자 대시보드 | 황정규 | VIEW-14 | pages/Admin | 담당 파트 설계 | admin | notices, inquiries |
| ADMIN-11 | 저격왕 전체 글 보기(관리자) | 황정규 | VIEW-19 | pages/Admin | 담당 파트 설계 | admin | pass_stories |
| CS-01 | 고객센터 메인 | 황정규 | VIEW-12 | pages/Support | 담당 파트 설계 | support | (저장 없음) |
| CS-02 | 공지사항 | 황정규 | VIEW-12 | pages/Support | 담당 파트 설계 | support | notices |
| CS-03 | FAQ | 황정규 | VIEW-12 | pages/Support | 담당 파트 설계 | support | common_codes, faqs |
| CS-04 | 1:1 문의하기 | 황정규 | VIEW-13 | pages/Support | 담당 파트 설계 | support | common_codes, inquiries |
| CS-05 | 나의 문의 내역 | 황정규 | VIEW-13 | pages/Support | 담당 파트 설계 | support | inquiries, inquiry_answers |
