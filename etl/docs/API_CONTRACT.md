# 추천·자격증 API 명세 (schema_version "3")

## 0. 엔드포인트

서버는 아래 7개 경로만 둡니다. 각 경로는 `jeogyeokjeung/service.py`의 함수 하나를 그대로 부르고, 함수가 돌려준 dict를 JSON으로 내보내면 됩니다.

| # | 화면 | 메서드 · 경로 | 부르는 함수 | 입력 |
|---|---|---|---|---|
| 1 | 정보 입력 드롭다운 · 자격증 검색 · 상세 탐색 | `GET /api/v1/options/{kind}` | `options(kind, school_id, query, offset, limit, qualification_type, series, interest_field_id, job_field_id)` | 경로 `kind` = schools · majors · grades · interest_fields · job_fields · qualifications / 쿼리 `school_id`(majors만 필수) · `query` · `offset`(기본 0) · `limit`(기본 50, 최대 100) · `type`(TECHNICAL · PROFESSIONAL) · `series`(qualifications만, 국가기술 등급) · `interest_field_id` · `job_field_id` |
| 2 | 전공 맞춤 추천 (처음 진입·새로고침, 회원정보 기준) | `POST /api/v1/recommendations` | `recommend(profile)` | 본문 = 회원정보의 대학교·전공·학년·보유 자격(2장). 응답 `items` 최대 5개 |
| 2-2 | 추천 (현재 선택 조건에서 직접 고름) | `POST /api/v1/recommendations/conditions` | `recommend_by_condition(conditions)` | 본문 = 자격구분·관심 분야·희망 직무(2-2장). 응답 `items` 최대 5개 |
| 3 | 자격증 상세 · 시험일정 · 합격률 | `GET /api/v1/qualifications/{jmcd}` | `qualification_detail(jmcd)` | 경로 `jmcd`(`1320` 또는 `QNET:1320`) |
| 4 | 트렌드 조회 | `GET /api/v1/trends` | `trends(qualification_type, series, interest_field_id, year, limit)` | 쿼리 `type`(TECHNICAL 기본 · PROFESSIONAL) · `series` · `interest_field_id` · `year` · `limit`(기본 20, 최대 100) |
| 5 | 내 시험 D-Day | `GET /api/v1/exam-schedules/{schedule_id}/dday` | `dday(schedule_id, exam_date=)` | 경로 `schedule_id`(상세의 `schedules_2026[].schedule_id`) / 쿼리 `exam_date`(시험 기간이 여러 날이면 필수) |
| 6 | 서버 상태 확인 | `GET /api/v1/health` | (없음) | 응답 `{"status": "ok", "schema_version": "3"}` |

공통 규칙
- 응답은 모두 `application/json; charset=utf-8`, 성공 200.
- 오류 본문은 팀 공통 형식 `{"code", "message", "timestamp"}`입니다(기능 명세서 10장).
  - 잘못된 입력: 함수가 `ValidationError` → **HTTP 400** `VALIDATION_ERROR`
  - 없는 종목코드·시험 회차: 함수가 `NotFoundError` → **HTTP 404** `NOT_FOUND`
  - 그 밖의 예외: **HTTP 500** `INTERNAL_ERROR`. 내부 메시지는 내보내지 않습니다.
- 기준일 `today`는 운영에서 넘기지 않습니다(서버 날짜, Asia/Seoul). 테스트에서만 함수 인자로 고정합니다.
- 인증은 팀 공통 규칙(JWT)을 따릅니다. 이 문서의 함수는 인증과 무관하게 입력만으로 결과를 계산합니다.
- 화면 쪽 정해진 것: 대학교·전공은 검색+선택 드롭다운(검색어 없이 열면 전체 목록), 관심분야·희망직무는 최대 5개 칩, 합격률 그래프는 점에 마우스를 올리면 응시자·합격자 수(`pass_rates[].examinees`·`passed`) 표시.

아래 1~4장은 각 함수의 입력·출력 상세입니다.

## 1. 선택 목록 — `service.options(kind, school_id=None, query='', offset=0, limit=50)`
경로: `GET /api/v1/options/{kind}?school_id=&query=&offset=&limit=`

입력 흐름: **대학교(검색·선택) → 전공(검색·선택)**. 학부 칸 없음. 검색어 없이 부르면 전체 목록, 검색어를 주면 이름 부분일치로 좁힘.

| kind | 필요한 값 | items 원소 |
|---|---|---|
| schools | - | `{id, name, campus, school_type}` |
| majors | school_id | `{id, name, parent_name, scope, label}` — 그 학교 전공 + '(학부 전체)' 항목. `scope`: `MAJOR`(전공 기준) / `FACULTY`(학부 이름 항목 → 소속 전공 합산). `label`: 화면 표시용 `전공 · 소속 학부`(소속 학부가 있을 때). `query`는 전공 이름과 소속 학부 이름 둘 다 검색(경상 → 경상학부·경영학·회계학). 정렬은 학부별로 묶어 학부 항목 먼저 |
| grades | - | `{id, name}` — `1`~`6`, `GRADUATED`, `NOT_ENROLLED` |
| interest_fields | (선택) `type` | `{id, name, count}` — Q-Net 직무분야(대분류). `count` = 그 분야 종목 수(`type`을 주면 그 구분만). 종목이 0개인 분야는 목록에 없음 |
| job_fields | (선택) `type`, `interest_field_id` | `{id, name, count}` — Q-Net 직무분야(중분류). `interest_field_id`를 주면 그 대분류 아래만 |
| qualifications | (선택) `type` = TECHNICAL · PROFESSIONAL | `{id, name, qualification_type, qualification_type_name, series}` — 613종(국가기술 513 · 국가전문 100). `type`을 주면 그 구분만, 안 주면 전체. `series`(국가기술 등급: 기능사·산업기사·기사·기능장·기술사·서비스)로 등급만 걸러 볼 수 있음(국가전문과 함께 주면 오류). `series`는 국가기술 등급(기능사·산업기사·기사·기능장·기술사·서비스), 국가전문은 null. `interest_field_id`·`job_field_id`로 직무분야별 종목만 |

자격증 상세 탐색(자격구분 → 직무분야 → 분류 → 종목): `interest_fields?type=TECHNICAL` → `job_fields?type=TECHNICAL&interest_field_id=21` → `qualifications?type=TECHNICAL&interest_field_id=21&job_field_id=211`.

응답: `{kind, items[], total, offset, limit}` · `limit` 1~100 · 이름 가나다순 · `query`는 이름 부분일치.

## 2. 전공 맞춤 추천 (처음 진입, 회원정보 기준) — `service.recommend(profile, today=None)`

추천 페이지에 처음 들어오거나 새로고침하면 회원정보의 **대학교·전공·학년(+보유 자격)** 으로 호출합니다. 희망 직무·관심 분야는 받지 않습니다(조건 선택 추천 2-2장에서만 사용).
경로: `POST /api/v1/recommendations`

입력(이 키 외에는 거부):
```json
{
  "school_id": "084d0e1ab9bcfde8",
  "major_id": "298669b03b67514f",
  "grade": "4",
  "owned_qualification_ids": []
}
```
- `school_id`·`major_id`·`grade`는 **셋 다 넣거나 셋 다 비웁니다**(회원가입에서 학교 정보 없음을 고른 회원). 하나만 있으면 400.
- 셋 다 비어 있으면 결과 없이 `needs_school_info: true` → 화면은 "관심 분야·희망 직무를 골라 주세요" 안내(조건 선택 추천으로 유도).
- `owned_qualification_ids`(선택): 보유 자격과 그 하위 등급은 결과에서 뺍니다.
- **화면 표시**: `items` = 학과 연결 추천(`recommended`) 점수순 **최대 5개**(`result_limit`). 학과 연결이 5개보다 적으면 그 수만큼만 나옵니다.
- **결과 0개 구분**: `needs_school_info: true` → 학교 정보 없음 안내 / `needs_school_info: false`이고 `items`가 빈 목록 → 학과 연결 없음 안내(관심 분야·희망 직무로 찾아보기). 두 경우 모두 화면은 조건 검색창을 펼칩니다.
- `major_id`는 `options majors`의 `id`(전공 ID 또는 '(학부 전체)' 항목의 학부 ID). 같은 학교여야 함. `scope`가 `FACULTY`인 항목이면 `recommendation_scope = FACULTY`(소속 전공 합산, `faculty_share` 포함).
- 합쳐진 동명 학과의 예전 `major_id`를 보내도 대표 ID로 바꿔 처리하고, 응답 `profile.major_id`에 대표 ID를 돌려줌.

출력:
```json
{
  "schema_version": "3",
  "qualification_scope": "QNET_MASTER_613",
  "profile": { ...검증된 입력... },
  "recommendation_scope": "MAJOR",
  "major_discipline": "빅데이터·AI",
  "recommended": [
    {
      "jmcd": "1320", "qualification_id": "QNET:1320", "name": "정보처리기사",
      "basis": "recommended",
      "exam_window": {"status": "NOW", "from_grade": 4, "related_major_required": false},
      "personal_eligibility_verified": false,
      "score": 60, "score_breakdown": [{"factor": "ACADEMIC_LINK", "points": 35, "reason": "전공 연관"}],
      "card": { ... }
    }
  ],
  "owned_excluded_count": 0,
  "grade_guidance": "학습 일정 수립 및 공식 응시요건 별도 확인",
  "exam_window_basis": "https://www.q-net.or.kr/crf006.do?id=crf00603&gSite=Q",
  "personal_eligibility_verified": false,
  "needs_school_info": false,
  "items": [ ...recommended 앞 최대 5개... ],
  "result_limit": 5
}
```
- `recommendation_scope`: `MAJOR`(전공 기준) | `FACULTY`(학부 이름 항목 선택 — 소속 전공 추천 합산) | `NONE`(학교 정보 없음).
- `FACULTY`: `recommended[].basis = faculty_aggregate`, 각 항목에 `faculty_share {majors, of}`(학부 소속 전공 of개 중 majors개에 연결). 정렬: 점수 → 겹침 많은 순.
- `major_category`: `{large, mid, small}` 대·중·소계열(학과 선택 시만, 아니면 null).
- **순위 점수**: 각 항목에 `score`(합계)와 `score_breakdown[] {factor, points, reason}`. 정렬은 `score` 내림차순. 점수표(`jeogyeokjeung/service.py`의 `SCORE`):
  학과 연결(직접 +40 / 전공연관 +35 / 동계열 +30 / 학부 합산 40×겹침비율) ·
  응시 시점(지금 가능·제한 없음 +20 / 다음 학년부터 +10 / 그 뒤 +5 / 개별 확인 +5 / 졸업·경력 후 0) ·
  `today` 이후 2026 남은 시험 회차 +10 · 지금 학년에 딸 수 있는 시작 등급 +5 (4년제: 1·2학년 기능사, 3학년 산업기사, 4학년·졸업 기사 / 전문대: 1학년 기능사, 2학년↑ 산업기사) · 재학생의 기술사·기능장 −20.
  `today`(YYYY-MM-DD)를 안 주면 서버 날짜. 응답 `score_basis_date`에 기준일.
- `recommended`: 학과에 연결된 자격증. `basis` = `existing_direct_links`(기존 직접연결) | `recommended`(전공연관 추천) | `same_subfield`(같은 소계열 학과 절반 이상이 가진 동계열 공통 자격). 점수 순, 같으면 연결 순위 순. 보유 자격증은 빠짐.
- `exam_window.status`: `NO_RESTRICTION`(제한 없음) · `NOW`(지금 응시 가능 학년) · `FROM_GRADE`(`from_grade`학년부터) · `AFTER_GRADUATION_EXPERIENCE`(졸업·경력 후) · `CHECK_QUALIFICATION`(국가전문자격 등 개별 확인).
- `exam_window.related_major_required`: 산업기사·기사·기술사·기능장은 기본 true. 단 Q-Net 시험정보 관련학과가 '모든 학과 응시가능'인 6종목(정보처리기사·정보처리산업기사·사무자동화산업기사·컴퓨터시스템기사·정보관리기술사·컴퓨터시스템응용기술사)은 false.
- `exam_window.via_owned_qualification`(있을 때만): 보유 자격만으로 바로 응시 가능할 때 그 보유 종목코드. 산업기사 = 같은 직무분야 다른 종목 산업기사 등급 이상 보유, 기사 = 기사 등급 이상, 기술사 = 다른 기술사(Q-Net 응시자격별 안내). '동일 및 유사 직무분야' = 직무분야(대분류) 같음 + 국가기술자격법 시행규칙 [별표 11의2] 유사 직무분야(2026-12-31까지 시행본). 예: 전기기사 보유 → 소방설비산업기사(전기분야) 바로 응시, 정보기술(211) 종목은 모든 직무분야 보유 자격 인정.
- 응시자격 판정(Q-Net 응시자격별 안내 기준): 서비스분야 2급(직업상담사·사회조사분석사·컨벤션기획사·소비자전문상담사)과 텔레마케팅관리사·멀티미디어콘텐츠제작전문가·스포츠경영관리사·이러닝운영관리사는 `NO_RESTRICTION`, 서비스분야 1급은 `AFTER_GRADUATION_EXPERIENCE`. 국가전문자격은 응시자격 원문에 '제한 없음'이 있으면 `NO_RESTRICTION`(100종 중 38), 아니면 `CHECK_QUALIFICATION`.
- `owned_excluded_count`: 추천에서 뺀 보유 자격과 그 하위 등급 수(예: 정보처리기사 보유 → 정보처리산업기사도 제외).
- `roadmap[]`: 학년별 로드맵. `{stage, label, count, items[{jmcd, name, score}]}` — `stage`는 지금 학년~최종 학년('1'~'4'), `GRADUATED`(졸업 후·실무경력), `CHECK`(개별 확인). 추천 종목을 처음 응시 가능한 학년에 배치, 단계마다 점수 상위 5개. 학년이 숫자가 아니면 `[]`.
- `next_levels[]`: 보유 자격의 상위 등급(같은 이름 계열). `{from_jmcd, from_name, jmcd, name, exam_window, experience_route}` — 국가기술 기능사→산업기사→기사→기능장/기술사, 서비스·국가전문 3급→2급→1급. `experience_route`는 보유 자격+실무경력으로 응시하는 경로 문구(학과 경로는 `exam_window`).
- `needs_school_info`: 대학교·전공·학년이 비어 있으면 true(결과 없음) → 화면은 조건 선택 추천 안내.
- `card`(항목마다): 추천 카드 표시값. `{qualification_type_name, series, field, summary, pass_rate_latest{exam_year, items[]}, next_registration, nearest_exam}`. 기준은 자격증 상세(3장)와 같고, 값이 없으면 null(미제공 표시).
- `roadmap`·`next_levels`는 기능 명세서 v1.4.0에서 화면 반영 미정이다(응답에는 계속 포함).

## 2-2. 조건 선택 추천 — `service.recommend_by_condition(conditions, today=None)`
경로: `POST /api/v1/recommendations/conditions`

현재 선택 조건에서 자격구분·관심 분야·희망 직무를 고르면 그 조건만으로 다시 추천합니다. **대학교·전공·학년은 받지 않고**(보내면 400), 학과 연결·응시 시점·학년 등급 같은 기존 합산 점수는 넣지 않습니다. 고른 조건은 저장하지 않으며, 다시 들어오면 회원정보 기준(2장)으로 시작합니다.

입력(이 키 외에는 거부):
```json
{ "qualification_type": "TECHNICAL", "interest_field_ids": ["21"], "job_field_ids": ["211"], "owned_qualification_ids": [] }
```
- `interest_field_ids` 또는 `job_field_ids` 중 하나 이상 필수(각 최대 5개). `qualification_type`(선택) TECHNICAL / PROFESSIONAL.
- `owned_qualification_ids`(선택, 회원정보 보유 자격): 점수에는 안 쓰고, 이미 가진 자격과 그 하위 등급만 결과에서 뺍니다.

점수: 희망직무 일치 +30 · 관심분야 일치 +20 · 올해 남은 시험 회차 +10 · 응시 조건 없음 +5. 동점이면 최근 완결 연도 1차 실제 응시자 많은 순 → 종목코드 순.

출력: `{schema_version, mode: "CONDITION", conditions, items[], total_candidates, result_limit, score_basis_date, score_rule}`
- `items`가 빈 목록이면 화면은 조건 변경 안내(고른 조건 유지).
- `items[]`(최대 5개, 후보가 적으면 그 수만큼) = `{jmcd, qualification_id, name, interest_matches[], score, score_breakdown[], recent_examinees{year, count}, eligibility_required, card}`. `card`는 2장과 같음.

## 3. 자격증 상세 — `service.qualification_detail(jmcd, today=None)`
경로: `GET /api/v1/qualifications/{jmcd}` (`1320` 또는 `QNET:1320`)

```json
{
  "jmcd": "1320", "qualification_id": "QNET:1320",
  "qualification": { ...qualifications.csv 한 행... },
  "schedules_2026": [ { "schedule_id": "1320:0", "exam_round": "1회", "exam_stage": "필기",
                        "exam_start_date": "2026-01-30", "exam_end_date": "2026-03-03",
                        "registration_start_date": "...", "registration_end_date": "...",
                        "pass_announcement_date": "", "source_type": "OFFICIAL_ROUNDS", ... } ],
  "schedule_status": "AVAILABLE",
  "pass_rates": [ { "exam_year": 2024, "exam_stage": "필기", "examinees": 0, "passed": 0,
                    "pass_rate_percent": 0.0, "source_type": "QNET_OFFICIAL", "source_url": "..." } ],
  "pass_rate_status": "AVAILABLE",
  "pass_rate_definition": "합격자수 / 실제 응시자수 × 100 (접수자 아님)"
}
```
- `pass_rates`는 연도 오름차순, 같은 해 안에서는 필기→1차→2차→3차→실기→면접→시험 순.
- `qualification`에는 conducting_agency·related_majors·exam_subjects·exam_method·pass_criteria·eligibility가 나뉘어 들어 있음(없으면 빈 문자열, 원문은 acquisition_method). 국가전문자격은 `related_ministry`(관련부처, Q-Net 공식 분류)와 `field_source`(직무분야 출처: QNET/MAPPED)도 표시. 자격증 소개는 `summary`(화면용 한 줄, 613종 모두 있음, 자체 작성)·`overview`(개요)·`job_description`(수행직무)·`career_outlook`(진로 및 전망), 원천에 없으면 빈 문자열.
- 일정마다 기준일(`today`, 없으면 서버 날짜) 대비 `exam_status`(ENDED 종료 / IN_PROGRESS 시험 기간 중 / UPCOMING 예정), `registration_status`(NOT_OPEN 접수 전 / OPEN 접수 중 / VACANCY_NOT_OPEN 빈자리 접수 전 / VACANCY_OPEN 빈자리 접수 중 / CLOSED 마감), `dday_label`(예정만 D-n, 지난 시험은 '종료'). 응답에 `next_schedule_id`(아직 접수할 수 있는 가장 가까운 예정 회차, 없으면 null), `remaining_2026_count`(종료 안 된 회차), `applicable_2026_count`(접수 가능 회차), `remaining_2026_note`, `schedule_basis_date`.
- **화면 표시 순서**
  - `next_registration`: 다음 접수 회차(접수 중·접수 전 회차 중 가장 가까운 것). `nearest_exam`: 가장 가까운 시험(진행 중 포함). 카드·상세 상단에 먼저 보여 주고, 없으면 null(올해 남은 회차 없음 → `remaining_2026_note` 안내).
  - `schedules_2026`은 진행 중·예정 회차를 시험일 가까운 순으로 먼저, 끝난 회차(`exam_status: ENDED`, `dday_label: 종료`)는 최근 회차부터 뒤에 둔다. '다른 회차 보기'는 이 목록을 그대로 펼친다.
  - `pass_rate_latest`: 가장 최근 연도의 단계별 합격률(`{exam_year, items[]}`)을 먼저 보여 주고, '자세히 보기'에서 `pass_rates` 전체 연도를 그래프·표로 보여 준다.
- 자료가 없으면 `schedule_status` / `pass_rate_status` = `NOT_ACQUIRED` (시험 없음으로 단정하지 않음).
- **상단 기본정보**(`qualification`): `eng_name`(영문명) · `related_ministry`(관련부처, 613종) · `conducting_agency`(시행기관) · `fee_written`·`fee_practical`(국가기술 수수료, 원) · `fee_note`(국가전문 수수료 안내 문구) · `schedule_type`(`정기` / `상시` — 상시 기능사는 회차표 대신 큐넷 상시 일정 안내).
- **응시 조건**(`eligibility`): `{required, summary, conditions[{type, text}], note, source_url, personal_eligibility_verified: false}`.
  - `required`: true(조건 필요) / false(조건 없음) / null(확인 필요 — 국가유산수리기능자 24종). 화면: false면 "응시 조건 없음", true면 `conditions`를 "다음 중 하나" 목록으로.
  - `type`: 학력 · 경력 · 자격 · 훈련 · 기타. 국가기술은 등급별 법정 기준(기사 10개, 산업기사 8개, 기능장 6개, 기술사 11개), 서비스 분야·국가전문은 종목별.
- **시험일정 표**(회차별 한 줄): `schedule_rounds[] {exam_round, schedule_ids[]}` 순서대로 `schedules_2026`에서 필기·실기(1차·2차…)를 찾아 한 줄로 그립니다. 일정마다 `vacancy_registration_start_date`·`vacancy_registration_end_date`(빈자리 접수, 없으면 빈 문자열).
  - `registration_status`에 `VACANCY_NOT_OPEN`(정기 접수 끝·빈자리 접수 전), `VACANCY_OPEN`(빈자리 접수 중) 추가. 둘 다 아직 접수 가능으로 보아 `next_registration` 후보에 들어감.
- **연도별 검정현황**: `pass_rate_years`(연도 드롭다운, 최신순) · `pass_rates[]`에 `applicants`(원서 접수자, 2021~2025 국가기술만, 없으면 null)와 `attendance_rate_percent`(응시율 = 응시자 ÷ 접수자 × 100) 추가. 차트(접수자·응시자·합격자)는 선택 연도의 필기·실기 행으로 그립니다.

## 3-2. 트렌드 — `service.trends(qualification_type='TECHNICAL', series=None, interest_field_id=None, year=None, limit=20, today=None)`
경로: `GET /api/v1/trends?type=TECHNICAL&series=기사&interest_field_id=21&limit=20`

- 기준: 최근 완결 연도(`today` 연도 이전 중 자료가 있는 마지막 해, 2026-11 기준 2025)의 **1차 실제 응시자 수**(필기 → 1차 → 단일시험 중 그 종목의 첫 단계). 2026 국가기술자격 통계연보 상위 종목 수치와 일치 확인.
- `year`에 올해(예: 2026)를 주면 **올해 누적(참고용)**: 결과가 발표된 회차만 합친 값, 응답 `partial: true`. 기본값(최근 완결 연도)은 `partial: false`.
- `qualification_type`: `TECHNICAL`(국가기술자격) | `PROFESSIONAL`(국가전문자격) — 따로 순위.
- `series`: 기능사 / 산업기사 / 기사 / 기술사 / 기능장(국가기술). `interest_field_id`: 관심분야(직무분야 대분류) 코드.
- 응답: `{year, qualification_type, basis, total, trend_rule, items[]}`, `items[]` = `{rank, jmcd, name, qualification_type, series, interest_field_id, interest_field, year, stage, examinees, passed, pass_rate_percent, five_year[{year, examinees}], five_year_change_percent, trend}`.
- **추천 페이지 '지금 주목받는 자격증'**: 같은 규칙에서 `limit=3`으로 불러 1~3위만 표시(`type` 기본 TECHNICAL, 화면에서 국가전문으로 바꾸면 `type=PROFESSIONAL`). 초기 메인 트렌드는 개수 별도.
- `trend`: 5년 전 대비 응시자 +10% 초과 `UP` / −10% 미만 `DOWN` / 그 사이 `STEADY` / 5년 자료 부족 `NEW_OR_SHORT_HISTORY`.
- 자격증 상세(`qualification_detail`)의 `trend`에도 같은 항목 + `rank_in_type`(같은 유형 안 순위), `type_total`.

## 4. D-Day — `service.dday(schedule_id, today=None, exam_date=None)`
경로: `GET /api/v1/exam-schedules/1320:0/dday?exam_date=2026-01-30`
- `schedule_id`: 이 구현은 `종목코드:순번` 문자열이고, Java에서는 `qualification_exam_schedules.id`(BIGINT)를 씁니다. 플래너의 목표 시험일 D-Day는 `target_exams.exam_date`로 같은 계산(`DdayCalculator`)을 씁니다.

- 시험 기간이 하루가 아니면 `exam_date`(기간 안의 실제 응시일) 필수.
- 응답: `{schedule_id, jmcd, exam_stage, exam_date, today, days_remaining, label, state}` — `label` = `D-5` / `D-Day` / `종료`(지난 시험은 응시 불가), `state` = UPCOMING / TODAY / ENDED.
- 주의: `schedule_id`는 데이터 재생성 시 바뀔 수 있으므로 DB 이관 시 일정 테이블의 PK로 대체.
