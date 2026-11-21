# 데이터 테이블 정의 (data/*.csv, UTF-8 BOM, 헤더 1행)

보정표·생성 스크립트(`tools/*.csv`, `tools/build_service_data.py`, `tools/faculty_scope`)는 원천 프로젝트(Planner-DataSchool-v2)에 있습니다.

CSV 파일 1개를 MySQL 테이블 하나에 적재합니다. 적재할 테이블 이름은 DB 설계서 Ver.12([ERD](../../docs/ERD.md))를 따릅니다. CSV의 원천 ID(school_id·faculty_scope_id·major_id)는 DB에 넣지 않고 적재 도구(`tools/load_service_data.py`)가 DB ID(1부터)로 바꿔 FK를 채웁니다. 아래 PK·FK는 CSV 기준이며 `tests/test_data_integrity.py`가 검사합니다.

| CSV | 적재 테이블 (DB 설계서 Ver.12) | PK | 컬럼 | FK |
|---|---|---|---|---|
| schools | universities (school_id → DB id) | school_id | school_name, campus, school_type | |
| majors | majors (major_id → DB id) | major_id | school_id, major_name, college_name, parent_name | school_id → schools |
| faculties | colleges (faculty_scope_id → DB id) | faculty_scope_id | school_id, faculty_name | school_id → schools |
| faculty_majors | majors.college_id (소속 학부) · 학부 이름과 같은 전공 행은 majors.aggregate_college_id · 학부 이름 전공이 없는 학부는 colleges.is_list_item = 1 | (faculty_scope_id, major_id) | | → faculties, → majors |
| qualifications | qualifications | jmcd | name, qualgbcd/qualgbnm(국가기술·국가전문), seriesnm(Q-Net 원장 등급. DB grade_level은 적재 시 종목명으로 판정), obligfldcd/obligfldnm(직무분야 대=관심분야), mdobligfldcd/mdobligfldnm(직무분야 중=희망직무), conducting_agency(시행처), related_majors(관련학과), exam_subjects(시험과목), exam_method(검정방법), pass_criteria(합격기준), eligibility(응시자격), related_ministry(관련부처, 613종), summary(화면용 한 줄 소개), summary_basis(QNET_TEXT / EXAM_INFO), overview(개요), job_description(수행직무), career_outlook(진로 및 전망), eng_name(영문명), fee_written/fee_practical(국가기술 필기·실기 수수료, 원), fee_note(국가전문 수수료 안내), schedule_type(정기/상시), eligibility_required(응시 조건 Y 필요 / N 없음 / 빈칸 확인 필요), eligibility_summary(한 줄 안내), eligibility_rule_key(조건 목록 키) | eligibility_rule_key → eligibility_rules(rule_key). obligfldcd·mdobligfldcd는 적재 시 interest_fields·job_categories로도 들어감 |
| major_qualifications | major_qualification_links | (major_id, jmcd) | link_rank(학과 안 순서), basis(existing_direct_links/recommended/same_subfield) | → majors, → qualifications |
| exam_schedules | qualification_exam_schedules | (jmcd, exam_stage, exam_start_date, exam_end_date) | exam_year(2026, DB에는 컬럼 없음, YEAR(exam_start_date)로 조회), exam_round, registration_start_date, registration_end_date(정기 접수), pass_announcement_date, vacancy_registration_start_date/vacancy_registration_end_date(빈자리 접수) | jmcd → qualifications |
| pass_rates | qualification_statistics_yearly | (jmcd, exam_year, exam_stage) (DB는 id AI, 이 묶음 UQ) | exam_year(2001~최근 완결 연도), applicants(원서 접수자, 2021~2025 국가기술만), examinees(실제 응시자), passed, source_type(QNET_PAGE/QNET_OFFICIAL/QNET_STATS_API), source_url | jmcd → qualifications |
| eligibility_rules | qualification_eligibility_rules | (rule_key, seq) (DB는 id AI, 이 묶음 UQ) | condition_type(학력·경력·자격·훈련·기타), condition_text | |
| daily_idioms | daily_idioms | display_order (DB는 id AI, idiom UQ) | idiom(사자성어), hanja(한자), meaning(뜻). 팀 제공 50개, 수집 파이프라인 대상 아님 | |

## 국가전문자격 직무분야 보완
- Q-Net 원장에서 국가전문자격 100종은 직무분야 코드가 비어 있다. `tools/professional_fields.csv`(자격 계열 seriesnm → 대·중분류 매핑표)로 98종을 채운다(QNET·MAPPED 건수는 `data/MANIFEST.json`의 `professional_field_mapping`). 국가기술자격에 쓰이는 코드만 쓰고, 맞는 중분류가 없으면 대분류만(세무사·관세사 등). 경비지도사 2종은 맞는 분야가 없어 빈칸.
- 관련부처 `related_ministry`: Q-Net 국가자격 종목별 상세정보(crf005)의 국가전문자격 '관련부처' 분류 그대로(18개 분류, 100종 전부). 같은 칸 `tools/professional_fields.csv`의 ministry. 입력값이 아니라 상세 화면 표시용.
- 효과: 관심분야·희망직무 점수와 트렌드 관심분야 필터에 국가전문자격도 잡힘.

## 서비스 범위
- 학부·전문대 과정만. 학교 유형에 '대학원'이 들어가는 기관(일반·특수·전문대학원) 소속 학과와 원천 학과 과정에 석사·박사가 들어가는 학과(전문대 전문기술석사 포함)는 제외. 범위·제외 수는 `data/MANIFEST.json`의 `service_scope`.
- schools 는 학부 학과가 1개 이상 남는 학교만, faculties 는 그 학교 것만, faculty_majors 는 남는 학과만.
- faculties·faculty_majors 는 화면 입력(대학교 → 전공)에는 쓰지 않고, 전공 목록의 학부 이름 항목을 골랐을 때 소속 전공 합산 추천에만 쓴다.

## 동명 코드 합치기
- 같은 학교·단과대학·학부·이름이면 한 줄. 대표 = 연결 자격증이 가장 많은 코드(같으면 major_id 순), 연결은 합집합. 나머지 ID는 서비스 데이터에 남기지 않는다(원천 프로젝트에만 있음). 합치기 전·후 학과 수는 `data/MANIFEST.json`의 `major_merge`.

## 학과명 보완 규칙
- 동명 코드를 합친 뒤, 기존 연결이 전혀 없는 학과에만 학과명 핵심어로 계열을 정해 그 계열 대표 자격증을 붙인다(추가 학과 수는 `data/MANIFEST.json`의 `name_rule_supplement`). 규칙 파일 `tools/major_name_rules.csv`(keyword, discipline, mode, exclude, note).

## 동계열 묶음
- 소계열(대·중·소 3단 키) 학과 5개 이상, 학과 절반 이상 공통, 기술사·기능장 제외, 최대 5개 → 빠진 학과에 `basis=same_subfield` 행 추가. 제외 소계열: 군사・국방・안보. 계열 출처: 학과_계열매핑.csv(대학알리미 공식 계열 중심).

## 출처 (수집 파이프라인 결과 파일 → 서비스 테이블)
- schools, majors: `data/serving/majors.csv` + `data/reference/kcue/program_reviews_2026.csv`(선택 제외 판정) + Stage3 `discipline`
- faculties, faculty_majors: `tools/faculty_scope.build_faculty_scopes`
- qualifications: `data/source/qnet/qualification_master.csv` + `data/curated/qualification_profile.csv`
- major_qualifications: 수집 파이프라인 `RecommendationProfileService`가 쓰는 Stage3 `final_build_result`의 `existing_direct_links`·`recommended` 중 613종 안의 것
- exam_schedules: `data/curated/exam_schedules/official_exam_rounds.csv` + `data/curated/qualification_exam_schedules_2026.csv` (같은 종목·단계·날짜는 1행). 국가전문자격은 설명의 `(N회 1차/2차/면접)`을 단계명으로 사용
- pass_rates: `data/curated/pass_rates/qnet_annual_pass_rates.csv` + `data/curated/qualification_statistics_2021_2025.csv`(앞 파일에 없는 연도·단계만)

## qualifications 설명 칸 나누기
- `qualification_profile.csv`의 `acquisition_method` 원문을 항목 이름(시행처·관련학과·시험과목·검정방법·합격기준·응시자격) 기준으로 나눔. 원문(`acquisition_method`)은 원천 프로젝트에만 둔다.
- 채워진 종목 수(613 중): 시행처 483, 관련학과 294, 시험과목 540, 검정방법 473, 합격기준 480, 응시자격 76. 국가전문자격 상당수는 원문 형식이 달라 비어 있음.
- 합격기준 뒤에 붙던 '작업형 실기시험 기본정보(안전등급 등)'는 잘라냄. `&middot;` 등 HTML 기호는 문자로 변환.

## 자격증 소개 문구(overview·job_description·career_outlook)
- 출처: 공공데이터포털「한국산업인력공단_국가기술자격 종목별 시험정보」(data.go.kr 3038404, 2025-12-31 기준) → `tools/qualification_texts.csv`.
- 채워진 종목 수(613 중): 개요 391, 수행직무 389, 진로 및 전망 217. 국가기술 105종(원천 내용 비어 있음)과 국가전문 100종은 빈칸.
- 원문은 PDF 줄바꿈 흔적으로 단어 중간 띄어쓰기가 있음(예: '필요 하다').
- `summary`: 화면 카드용 한 줄 소개(예: '소프트웨어의 기획·분석·설계·구현과 정보시스템 관리 능력을 평가하는 자격증이에요.'). 613종 전부 서비스 작성 문구(공식 문구 아님). `summary_basis` = QNET_TEXT(공단 수행직무 근거, 389종) / EXAM_INFO(수행직무 없음 → 시험과목·직무분야 근거, 224종. 개요만 있는 종목은 제정 목적만 담겨 있어 근거로 보지 않음). 공식 문구로 바꿀 때는 `tools/qualification_texts.csv`만 고치고 빌드. 검토 목록: `docs/qualification_summary_review.xlsx`(EXAM_INFO 224종).

## Q-Net 종목 상세 페이지 반영 (2026-11-18 수집)
- 원본: 수집 파이프라인 `data/source/qnet/qnet_detail_pages_2026-11-18.json` — 613종 큐넷 종목 상세(crf00503 기본정보, crf00503s02 시험정보, crf00503s01 검정현황)를 브라우저로 모은 것.
- qualifications: 영문명 595종, 관련부처 613종(여러 부처는 `, `로 구분, 큐넷 표기 그대로), 수수료 국가기술 필기 491·실기 509종, 국가전문 수수료 안내 87종, 상시 시험 12종.
- exam_schedules: 큐넷 시험일정 표의 `[빈자리접수 : …]`를 같은 종목·단계·시험 시작일 행에 붙임(1,788행). 정기 접수 칸에 빈자리 기간이 들어가 있거나 정기~빈자리가 한 기간으로 묶여 있던 1,396행은 큐넷 표의 정기 접수 기간으로 바로잡음.
- pass_rates: 큐넷 '종목별 검정현황' 표 전 연도(2001~2025, `QNET_PAGE`)를 먼저 쓰고, 없는 연도·단계만 기존 원천(`QNET_OFFICIAL`, 국가전문 통계 게시판 등)으로 채움. `applicants`는 2026 국가기술자격 통계연보(2021~2025)에서 가져오며, 같은 행의 통계연보 응시자 수와 큐넷 응시자 수가 모두 일치(불일치 0).

## 응시 조건 (eligibility_rules)
- 국가기술: 등급별 법정 기준(국가기술자격법 시행령 별표 4의2, 큐넷 '응시자격별 안내' 문구) — `GRADE:산업기사`(8)·`GRADE:기사`(10)·`GRADE:기능장`(6)·`GRADE:기술사`(11). 기능사·기능사보·미용사·이용사 등 기능사 등급은 조건 없음.
- 서비스 분야: 2급·텔레마케팅관리사·멀티미디어콘텐츠제작전문가·스포츠경영관리사·이러닝운영관리사·공공조달관리사는 조건 없음, 1급·임상심리사·국제의료관광코디네이터는 `Q:종목코드`.
- 국가전문: 큐넷 응시자격이 '제한 없음'이거나 결격사유만 있으면 조건 없음, 학력·경력 요건이 있는 14종은 `Q:종목코드`. 국가유산수리기술자 6종은 요건 있음(조건 목록 없음), 국가유산수리기능자 24종은 큐넷에 응시자격 안내가 없어 확인 필요(빈칸).
- 조건 문구는 `tools/eligibility_rules.csv`(원본)를 고치고 빌드하면 바뀜. 개인 응시 가능 여부를 확정하지 않음(관련학과·경력 인정은 큐넷 자가진단).

## 서비스 데이터에서 뺀 원천 컬럼
- 빌드 계산에는 쓰지만 서비스·DB에서 쓰지 않는 컬럼은 CSV로 내보내지 않는다(`tools/build_service_data.py`의 `WORK_ONLY`).
- majors: degree_course(학위 과정은 universities.school_type), academic_category, unit_type, merged_major_ids, discipline·category_large/mid/small(학과 계열: 학과→자격증 연결을 만들 때만 사용)
- faculties: resolution
- major_qualifications: link_basis, recommendation_type
- exam_schedules: source_note, source_type·source_url(시험일정 화면에 출처 표시 없음)
- pass_rates: 2001년 이전·진행 중인 올해 행(명세서 CF-02-04 기준 2001년~)
- qualifications: seriescd, acquisition_method, exam_trend, field_source
