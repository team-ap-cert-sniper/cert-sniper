# 🗂 ERD

| 쓰는 곳 | 방법 |
|---|---|
| GitHub · Notion | 아래 `mermaid` 코드 블록이 그대로 그림으로 보입니다. Notion은 코드 블록을 만들고 언어를 Mermaid로 고른 뒤 붙여 넣습니다 |
| ERD 프로그램 | [V1__create_tables.sql](../backend/src/main/resources/db/migration/V1__create_tables.sql)을 SQL 가져오기(ERDCloud·dbdiagram·draw.io 등)에 넣거나, MySQL에 실행한 뒤 DBeaver의 ER 다이어그램으로 엽니다 |
| 기능 ID에서 테이블 찾기 | [TRACEABILITY.md](TRACEABILITY.md) |

관계 표기: `||--o{` 1:N (부모 필수) · `|o--o{` 0..1:N (부모 선택) · `||--o|` 1:1 · 점선은 논리 참조(FK 없음). 선 위 이름은 자식 테이블의 FK 컬럼입니다.

키: 모든 테이블은 단일 PK이고 FK도 단일 컬럼입니다. 이메일 인증코드는 DB에 저장하지 않습니다(백엔드 메모리).

## 전체 관계

```mermaid
erDiagram
    users["회원 (users)"]
    user_tokens["로그인 유지 토큰 (user_tokens)"]
    user_certificates["보유 자격증 (user_certificates)"]
    account_status_histories["계정 상태 이력 (account_status_histories)"]
    universities["대학 (universities)"]
    colleges["학부·단과대학 (colleges)"]
    majors["학과·전공 (majors)"]
    job_categories["희망 직무 (job_categories)"]
    interest_fields["관심 분야 (interest_fields)"]
    major_qualification_links["학과–자격증 연결 (major_qualification_links)"]
    qualifications["자격증 Master (qualifications)"]
    qualification_statistics_yearly["연도·단계별 합격 통계 (qualification_statistics_yearly)"]
    qualification_exam_schedules["시험일정 (qualification_exam_schedules)"]
    qualification_eligibility_rules["응시 조건 목록 (qualification_eligibility_rules)"]
    favorites["관심 자격증 (favorites)"]
    target_exams["목표 자격증 (target_exams)"]
    planners["플래너 다이어리 (planners)"]
    personal_schedules["개인 일정 (personal_schedules)"]
    planner_bars["일정바 (planner_bars)"]
    planner_milestones["세부 일정·마일스톤 (planner_milestones)"]
    memos["자격증별 메모 (memos)"]
    daily_idioms["오늘의 사자성어 (daily_idioms)"]
    pass_stories["합격수기 (pass_stories)"]
    pass_story_reports["합격수기 신고 (pass_story_reports)"]
    pass_story_moderation_logs["게시글 상태 변경 이력 (pass_story_moderation_logs)"]
    user_ranks["회원 저격 등급 (user_ranks)"]
    notices["공지사항 (notices)"]
    faqs["FAQ (faqs)"]
    inquiries["1:1 문의 (inquiries)"]
    inquiry_answers["문의 답변 (inquiry_answers)"]
    admin_audit_logs["관리자 변경 이력 (admin_audit_logs)"]
    universities |o--o{ users : "university_id"
    colleges |o--o{ users : "college_id"
    majors |o--o{ users : "major_id"
    users ||--o{ user_tokens : "user_id"
    users ||--o{ user_certificates : "user_id"
    qualifications |o--o{ user_certificates : "jmcd"
    users ||--o{ account_status_histories : "user_id"
    users |o--o{ account_status_histories : "admin_id"
    universities ||--o{ colleges : "university_id"
    universities ||--o{ majors : "university_id"
    colleges |o--o{ majors : "college_id"
    colleges |o--o{ majors : "aggregate_college_id"
    interest_fields ||--o{ job_categories : "interest_field_code"
    majors ||--o{ major_qualification_links : "major_id"
    qualifications ||--o{ major_qualification_links : "jmcd"
    interest_fields |o--o{ qualifications : "obligfldcd"
    job_categories |o--o{ qualifications : "mdobligfldcd"
    qualifications ||--o{ qualification_statistics_yearly : "jmcd"
    qualifications ||--o{ qualification_exam_schedules : "jmcd"
    users ||--o{ favorites : "user_id"
    qualifications ||--o{ favorites : "jmcd"
    users ||--o{ target_exams : "user_id"
    qualifications ||--o{ target_exams : "jmcd"
    qualification_exam_schedules |o--o{ target_exams : "exam_schedule_id"
    users ||--o| planners : "user_id"
    users ||--o{ personal_schedules : "user_id"
    users ||--o{ planner_bars : "user_id"
    target_exams |o--o{ planner_bars : "target_exam_id"
    personal_schedules |o--o{ planner_bars : "personal_schedule_id"
    planner_bars ||--o{ planner_milestones : "bar_id"
    users ||--o{ planner_milestones : "user_id"
    target_exams ||--o| memos : "target_exam_id"
    users ||--o{ pass_stories : "user_id"
    qualifications ||--o{ pass_stories : "jmcd"
    pass_stories ||--o{ pass_story_reports : "story_id"
    users ||--o{ pass_story_reports : "reporter_id"
    pass_stories ||--o{ pass_story_moderation_logs : "story_id"
    users ||--o{ pass_story_moderation_logs : "actor_id"
    pass_story_reports |o--o{ pass_story_moderation_logs : "report_id"
    users ||--o| user_ranks : "user_id"
    users ||--o{ notices : "author_id"
    users ||--o{ inquiries : "user_id"
    inquiries ||--o| inquiry_answers : "inquiry_id"
    users ||--o{ inquiry_answers : "admin_id"
    users ||--o{ admin_audit_logs : "admin_id"
    users |o--o{ admin_audit_logs : "target_user_id"
    qualification_eligibility_rules |o..o{ qualifications : "eligibility_rule_key"
```

## 3장 회원·인증

```mermaid
erDiagram
    users["회원 (users)"] {
        bigint id PK "회원 ID"
        varchar email UK "이메일(아이디)"
        varchar password "비밀번호(BCrypt 해시)"
        varchar name "이름"
        varchar nickname UK "닉네임"
        varchar role "권한"
        varchar status "계정 상태"
        bigint university_id FK "대학 ID"
        bigint college_id FK "학부(단과대학) ID"
        bigint major_id FK "학과 ID"
        varchar grade "학년"
        int confirmed_hidden_count "누적 숨김 횟수"
        datetime suspended_until "정지 종료 일시"
        datetime withdrawal_requested_at "탈퇴 신청 일시"
        datetime created_at "가입 일시"
    }
    user_tokens["로그인 유지 토큰 (user_tokens)"] {
        bigint id PK "토큰 ID"
        bigint user_id FK "회원 ID"
        char token_hash UK "토큰 해시"
        char token_family "토큰 계열 ID"
        datetime expires_at "만료 일시"
    }
    user_certificates["보유 자격증 (user_certificates)"] {
        bigint id PK "보유 자격증 ID"
        bigint user_id FK "회원 ID"
        varchar jmcd FK "자격 종목코드"
        varchar cert_name "자격증명(직접 입력)"
    }
    account_status_histories["계정 상태 이력 (account_status_histories)"] {
        bigint id PK "이력 ID"
        bigint user_id FK "회원 ID"
        varchar change_type "변경 유형"
        varchar previous_status "변경 전 상태"
        varchar new_status "변경 후 상태"
        varchar reason "사유"
        datetime ends_at "정지 종료 일시"
        bigint admin_id FK "처리 관리자"
        datetime created_at "변경 일시"
    }
    universities |o--o{ users : "university_id"
    colleges |o--o{ users : "college_id"
    majors |o--o{ users : "major_id"
    users ||--o{ user_tokens : "user_id"
    users ||--o{ user_certificates : "user_id"
    qualifications |o--o{ user_certificates : "jmcd"
    users ||--o{ account_status_histories : "user_id"
    users |o--o{ account_status_histories : "admin_id"
```

## 4장 대학·학과·직무·추천

```mermaid
erDiagram
    universities["대학 (universities)"] {
        bigint id PK "대학 ID"
        varchar name "대학명"
        varchar campus_name "캠퍼스명"
        varchar school_type "학교 유형"
    }
    colleges["학부·단과대학 (colleges)"] {
        bigint id PK "단과대학 ID"
        bigint university_id FK "대학 ID"
        varchar name "학부·단과대학명"
        tinyint is_list_item "학부 전체 항목"
    }
    majors["학과·전공 (majors)"] {
        bigint id PK "학과 ID"
        bigint university_id FK "대학 ID"
        bigint college_id FK "소속 학부 ID"
        varchar name "학과·전공명"
        varchar parent_name "소속 표시명"
        bigint aggregate_college_id FK "합산 대상 학부 ID"
    }
    job_categories["희망 직무 (job_categories)"] {
        varchar code PK "직무 코드"
        varchar name "직무명"
        varchar interest_field_code FK "관심 분야 코드"
    }
    interest_fields["관심 분야 (interest_fields)"] {
        varchar code PK "관심 분야 코드"
        varchar name "관심 분야명"
    }
    major_qualification_links["학과–자격증 연결 (major_qualification_links)"] {
        bigint id PK "연결 ID"
        bigint major_id FK "학과 ID"
        varchar jmcd FK "자격 종목코드"
        smallint link_rank "연결 순서"
        varchar basis "연결 근거 유형"
    }
    universities ||--o{ colleges : "university_id"
    universities ||--o{ majors : "university_id"
    colleges |o--o{ majors : "college_id"
    colleges |o--o{ majors : "aggregate_college_id"
    interest_fields ||--o{ job_categories : "interest_field_code"
    majors ||--o{ major_qualification_links : "major_id"
    qualifications ||--o{ major_qualification_links : "jmcd"
```

## 5장 자격증

```mermaid
erDiagram
    qualifications["자격증 Master (qualifications)"] {
        varchar jmcd PK "자격 종목코드"
        varchar jmfldnm "자격 종목명"
        varchar eng_name "영문명"
        varchar qualgbcd "자격구분 코드"
        varchar grade_level "등급"
        varchar obligfldcd FK "관심 분야 코드"
        varchar mdobligfldcd FK "희망 직무 코드"
        varchar related_ministry "관련부처"
        varchar summary "한 줄 소개"
        varchar summary_basis "소개 근거"
        text overview "개요"
        text job_description "수행직무"
        text career_outlook "진로 및 전망"
        varchar conducting_agency "시행처"
        text related_majors "관련학과"
        text exam_method "검정방법"
        text exam_subjects "시험과목"
        text exam_standard "합격 기준"
        text eligibility "응시자격"
        char eligibility_required "응시 조건 여부"
        varchar eligibility_summary "응시 조건 안내"
        varchar eligibility_rule_key "응시 조건 키"
        int fee_written "필기 수수료"
        int fee_practical "실기 수수료"
        varchar fee_note "수수료 안내"
        varchar schedule_type "시험 형태"
    }
    qualification_statistics_yearly["연도·단계별 합격 통계 (qualification_statistics_yearly)"] {
        bigint id PK "통계 ID"
        varchar jmcd FK "자격 종목코드"
        smallint year "기준 연도"
        varchar exam_stage "시험 단계"
        int applicants "접수자 수"
        int examinees "응시자 수"
        int passers "합격자 수"
        decimal pass_rate_pct "합격률(%)"
        varchar data_source "통계 출처"
        varchar source_url "출처 URL"
    }
    qualification_exam_schedules["시험일정 (qualification_exam_schedules)"] {
        bigint id PK "일정 ID"
        varchar jmcd FK "자격 종목코드"
        varchar exam_round "회차"
        varchar exam_stage "시험 단계"
        date registration_start_date "정기 접수 시작일"
        date registration_end_date "정기 접수 종료일"
        date vacancy_registration_start_date "빈자리 접수 시작일"
        date vacancy_registration_end_date "빈자리 접수 종료일"
        date exam_start_date "시험 시작일"
        date exam_end_date "시험 종료일"
        date pass_announcement_date "합격 발표일"
    }
    qualification_eligibility_rules["응시 조건 목록 (qualification_eligibility_rules)"] {
        bigint id PK "조건 ID"
        varchar rule_key "응시 조건 키"
        tinyint seq "조건 순서"
        varchar condition_type "조건 구분"
        varchar condition_text "조건 내용"
    }
    interest_fields |o--o{ qualifications : "obligfldcd"
    job_categories |o--o{ qualifications : "mdobligfldcd"
    qualifications ||--o{ qualification_statistics_yearly : "jmcd"
    qualifications ||--o{ qualification_exam_schedules : "jmcd"
    qualification_eligibility_rules |o..o{ qualifications : "eligibility_rule_key"
```

## 6장 개인화 (찜·목표 자격증·플래너·메모)

```mermaid
erDiagram
    favorites["관심 자격증 (favorites)"] {
        bigint id PK "찜 ID"
        bigint user_id FK "회원 ID"
        varchar jmcd FK "자격 종목코드"
        datetime created_at "찜 일시"
    }
    target_exams["목표 자격증 (target_exams)"] {
        bigint id PK "목표 자격증 ID"
        bigint user_id FK "회원 ID"
        varchar jmcd FK "자격 종목코드"
        bigint exam_schedule_id FK "선택한 시험 회차"
        date exam_date "목표 시험일"
        char display_color "일정 색상"
        datetime created_at "등록 일시"
    }
    planners["플래너 다이어리 (planners)"] {
        bigint user_id PK,FK "회원 ID"
        varchar mode "화면 상태"
    }
    personal_schedules["개인 일정 (personal_schedules)"] {
        bigint id PK "개인 일정 ID"
        bigint user_id FK "회원 ID"
        varchar name "일정 이름"
        char display_color "일정 색상"
        datetime created_at "등록 일시"
    }
    planner_bars["일정바 (planner_bars)"] {
        bigint id PK "일정바 ID"
        bigint user_id FK "회원 ID"
        bigint target_exam_id FK "목표 자격증 ID"
        bigint personal_schedule_id FK "개인 일정 ID"
        varchar title "일정명"
        date start_date "시작일"
        date end_date "종료일"
        tinyint is_single_day "당일 일정 여부"
        varchar source "생성 방식"
    }
    planner_milestones["세부 일정·마일스톤 (planner_milestones)"] {
        bigint id PK "세부 일정 ID"
        bigint bar_id FK "일정바 ID"
        bigint user_id FK "회원 ID"
        date point_date "날짜"
        varchar content "내용"
        tinyint is_rest "휴식 여부"
        varchar source "생성 방식"
    }
    memos["자격증별 메모 (memos)"] {
        bigint id PK "메모 ID"
        bigint target_exam_id FK,UK "목표 자격증 ID"
        varchar content_text "메모 텍스트"
        text content_html "서식 본문"
        int version "버전"
        tinyint is_deleted "삭제 여부"
    }
    daily_idioms["오늘의 사자성어 (daily_idioms)"] {
        varchar idiom UK "사자성어"
        varchar hanja "한자 표기"
        varchar meaning "뜻"
        int display_order PK "순환 순서"
    }
    users ||--o{ favorites : "user_id"
    qualifications ||--o{ favorites : "jmcd"
    users ||--o{ target_exams : "user_id"
    qualifications ||--o{ target_exams : "jmcd"
    qualification_exam_schedules |o--o{ target_exams : "exam_schedule_id"
    users ||--o| planners : "user_id"
    users ||--o{ personal_schedules : "user_id"
    users ||--o{ planner_bars : "user_id"
    target_exams |o--o{ planner_bars : "target_exam_id"
    personal_schedules |o--o{ planner_bars : "personal_schedule_id"
    planner_bars ||--o{ planner_milestones : "bar_id"
    users ||--o{ planner_milestones : "user_id"
    target_exams ||--o| memos : "target_exam_id"
```

## 7장 저격왕 (합격수기·신고·등급)

```mermaid
erDiagram
    pass_stories["합격수기 (pass_stories)"] {
        bigint id PK "게시글 ID"
        bigint user_id FK "작성 회원 ID"
        varchar jmcd FK "합격 자격 종목코드"
        varchar body "상세 후기·실전 팁"
        varchar examinee_base "응시자 베이스"
        varchar prep_period "준비 기간"
        varchar attempt_band "응시 횟수"
        varchar textbook_name "사용 교재명"
        decimal pass_score "합격 점수"
        date exam_date "시험 응시일"
        varchar status "게시 상태"
        varchar hidden_reason "숨김 사유"
        datetime hidden_confirmed_at "숨김 확정 일시"
        datetime created_at "등록 일시"
    }
    pass_story_reports["합격수기 신고 (pass_story_reports)"] {
        bigint id PK "신고 ID"
        bigint story_id FK "게시글 ID"
        bigint reporter_id FK "신고 회원 ID"
        varchar report_type "신고 사유"
        varchar status "처리 상태"
        datetime created_at "신고 일시"
    }
    pass_story_moderation_logs["게시글 상태 변경 이력 (pass_story_moderation_logs)"] {
        bigint id PK "이력 ID"
        bigint story_id FK "게시글 ID"
        varchar action "조치"
        bigint actor_id FK "처리자 ID"
        varchar actor_role "처리자 구분"
        bigint report_id FK "관련 신고 ID"
        varchar reason_code "사유 코드"
        varchar reason_text "관리자 메모"
        varchar previous_status "변경 전 상태"
        varchar new_status "변경 후 상태"
        json previous_content "수정 전 내용"
        datetime created_at "처리 일시"
    }
    user_ranks["회원 저격 등급 (user_ranks)"] {
        bigint user_id PK,FK "회원 ID"
        int valid_cert_count "유효 합격 자격증 수"
        varchar current_rank "현재 등급"
    }
    users ||--o{ pass_stories : "user_id"
    qualifications ||--o{ pass_stories : "jmcd"
    pass_stories ||--o{ pass_story_reports : "story_id"
    users ||--o{ pass_story_reports : "reporter_id"
    pass_stories ||--o{ pass_story_moderation_logs : "story_id"
    users ||--o{ pass_story_moderation_logs : "actor_id"
    pass_story_reports |o--o{ pass_story_moderation_logs : "report_id"
    users ||--o| user_ranks : "user_id"
```

## 8장 고객센터·운영

```mermaid
erDiagram
    notices["공지사항 (notices)"] {
        bigint id PK "공지 ID"
        bigint author_id FK "작성 관리자"
        varchar notice_type "공지 유형"
        varchar title "제목"
        text content "본문"
        tinyint is_deleted "삭제 여부"
        datetime created_at "작성 일시"
    }
    faqs["FAQ (faqs)"] {
        bigint id PK "FAQ ID"
        varchar question "질문"
        text answer "답변"
        smallint sort_order "정렬 순서"
    }
    inquiries["1:1 문의 (inquiries)"] {
        bigint id PK "문의 ID"
        bigint user_id FK "작성 회원 ID"
        varchar inquiry_type "문의 유형"
        varchar title "문의 제목"
        varchar content "문의 내용"
        varchar status "처리 상태"
        datetime created_at "접수 일시"
    }
    inquiry_answers["문의 답변 (inquiry_answers)"] {
        bigint id PK "답변 ID"
        bigint inquiry_id FK,UK "문의 ID"
        bigint admin_id FK "답변 관리자"
        varchar content "답변 내용"
    }
    admin_audit_logs["관리자 변경 이력 (admin_audit_logs)"] {
        bigint id PK "로그 ID"
        bigint admin_id FK "관리자 ID"
        bigint target_user_id FK "대상 회원 ID"
        varchar action "작업"
        varchar target_table "대상 테이블"
        varchar target_id "대상 키"
        json before_json "변경 전 값"
        json after_json "변경 후 값"
        varchar reason "처리 사유"
        char request_id "요청 ID"
        varchar client_ip "접속 IP"
        datetime created_at "처리 일시"
    }
    users ||--o{ notices : "author_id"
    users ||--o{ inquiries : "user_id"
    inquiries ||--o| inquiry_answers : "inquiry_id"
    users ||--o{ inquiry_answers : "admin_id"
    users ||--o{ admin_audit_logs : "admin_id"
    users |o--o{ admin_audit_logs : "target_user_id"
```
