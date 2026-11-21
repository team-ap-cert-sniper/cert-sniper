# 🔄 저격증 데이터·추천 로직 (etl)

공공데이터로 만든 **서비스용 데이터(CSV 9종)** 와 팀이 정한 사자성어 목록(CSV 1종), 그 데이터를 쓰는 **추천·자격증 정보 로직(Python)** 을 담고 있습니다.

* **DB 적재 원천**: `data/*.csv`를 DB 설계서 Ver.12 테이블에 적재합니다 (CSV ↔ 테이블 대응은 `docs/DATA_TABLES.md`)
* **백엔드 이식 기준**: `jeogyeokjeung/`의 함수 흐름과 `docs/API_CONTRACT.md`의 입출력을 Spring Boot로 옮깁니다
* **결과 검증 기준**: `tests/golden_recommendations.json`과 같은 입력을 넣어 Java 결과가 같은지 비교합니다

Python 3.9 이상, 외부 라이브러리 없음. 담당: 이은아.

---

## 📦 구성

```
📁 etl/
├─ 📄 README.md
├─ 📄 .env.example                      공공데이터포털 인증키 자리 (실제 값은 .env, 커밋 금지)
├─ 📄 run.py                            명령줄 실행 도구 (확인·디버깅용)
│
├─ 📁 jeogyeokjeung/                    추천·자격증 정보 로직 (백엔드 이식 기준)
│  ├─ 📄 __init__.py
│  ├─ 📄 service.py                     선택 목록·추천·자격증 상세·트렌드·D-Day·오늘의 사자성어·입력 검증
│  ├─ 📄 eligibility.py                 학교 종류·학년·보유 자격별 응시 가능 시점 판정
│  └─ 📄 store.py                       data/*.csv 로딩 (DB 사용 시 이 모듈만 교체)
│
├─ 📁 data/                             서비스용 테이블 (UTF-8 BOM CSV, DB 적재 원천)
│  ├─ 📄 schools.csv                    학교·캠퍼스
│  ├─ 📄 majors.csv                     학과·전공
│  ├─ 📄 faculties.csv                  학부
│  ├─ 📄 faculty_majors.csv             학부 소속 학과
│  ├─ 📄 qualifications.csv             자격증 613종 (시험정보·영문명·관련부처·수수료·응시 조건·한 줄 소개)
│  ├─ 📄 major_qualifications.csv       학과 → 자격증 연결과 근거
│  ├─ 📄 exam_schedules.csv             2026 시험일정 (정기·빈자리 접수)
│  ├─ 📄 pass_rates.csv                 연도·단계별 접수자·응시자·합격자 (2001~2025)
│  ├─ 📄 eligibility_rules.csv          응시 조건 목록 (등급별·종목별)
│  ├─ 📄 daily_idioms.csv               오늘의 사자성어 50개 (팀 제공, 1~50번 하루 1개씩 순환)
│  └─ 📄 MANIFEST.json                  행 수·체크섬·빌드 통계
│  
│
├─ 📁 tools/
│  └─ 📄 load_service_data.py           빈 DB 초기 적재 SQL 생성 (CSV 10종, 실행할 때만 만들고 레포에 두지 않음)
│
├─ 📁 tests/                            테스트
│  ├─ 📄 __init__.py
│  ├─ 📄 test_data_integrity.py         테이블 PK·FK·행 수·체크섬 검사
│  ├─ 📄 test_service.py                추천·상세·트렌드·D-Day·입력 검증
│  └─ 📄 golden_recommendations.json    기준 추천 결과 (Java 이식 결과 비교용)
│
└─ 📁 docs/
   ├─ 📄 DATA_TABLES.md                 테이블 컬럼·PK·FK·출처
   └─ 📄 API_CONTRACT.md                API 경로·입출력 형식·화면 표시 순서
```

---

## 🗂 데이터 (`data/`)

| 파일 | 행 수 | 내용 |
|---|---|---|
| `schools.csv` | 539 | 학교·캠퍼스, 학제 |
| `majors.csv` | 21,730 | 학부·전문대 학과 (폐지·개칭·대학원 제외, 동명 코드 합침) |
| `faculties.csv` / `faculty_majors.csv` | 3,551 / 3,535 | 학부와 소속 학과 (학부 이름으로 고를 때 합산 추천에 사용) |
| `qualifications.csv` | 613 | Q-Net 자격증 (국가기술 513 · 국가전문 100), 시험정보·영문명·관련부처·수수료·응시 조건(필요 361 · 없음 228 · 확인 필요 24)·한 줄 소개·개요·수행직무 |
| `major_qualifications.csv` | 41,707 | 학과 → 자격증 연결과 근거 (직접 연결 · 전공 연관 · 동계열 공통) |
| `exam_schedules.csv` | 3,041 | 2026 시험일정 (회차·단계별 정기·빈자리 접수·시험·발표일) |
| `pass_rates.csv` | 22,313 | 연도·시험 단계별 접수자(2021~2025 국가기술)·실제 응시자·합격자, 2001~2025년 |
| `eligibility_rules.csv` | 124 | 응시 조건 목록 (국가기술 등급별 · 서비스 분야·국가전문 종목별) |

테이블 컬럼과 PK·FK는 [docs/DATA_TABLES.md](docs/DATA_TABLES.md)에 있습니다.

### 원천 데이터

| 원천 | 쓰인 곳 |
|---|---|
| 한국대학교육협의회 대학·학과 정보 (+ 학교 공식 홈페이지 검증) | schools, majors, faculties |
| 한국산업인력공단 Q-Net 국가자격 종목·상세정보 | qualifications |
| 한국산업인력공단 시험일정 (Q-Net 공식 회차표 + 공공데이터 API + 종목 상세 일정표) | exam_schedules |
| 한국산업인력공단 Q-Net 종목 상세 페이지 (2026-11-18 수집) | qualifications 영문명·관련부처·수수료·응시 조건, exam_schedules 빈자리 접수, pass_rates 전 연도 |
| 2026 국가기술자격 통계연보 | pass_rates 접수자 수 |
| Q-Net 응시자격별 안내 | eligibility_rules |
| 한국산업인력공단 검정현황·통계 (Q-Net 공식 + 공공데이터 API) | pass_rates |
| 공공데이터포털 「국가기술자격 종목별 시험정보」 | qualifications 개요·수행직무·진로 및 전망 |
| 대학알리미 학과 계열 분류 | major_qualifications 동계열 공통 자격(빌드 시) |

공공데이터 API 수집과 학과 검증은 **별도 수집 파이프라인**에서 실행하고, 이 폴더에는 그 결과를 정리한 CSV만 둡니다.

---

## ⚙️ 추천 로직 요약

추천은 두 가지이고, 결과는 두 추천 모두 **최대 5개**이고, 후보가 적으면 그 수만큼 나옵니다. 추천 페이지에 들어오거나 새로고침하면 ①로 시작하고, 고른 조건은 저장하지 않습니다.

**① 전공 맞춤 추천** (`recommend`, 회원정보의 대학교·전공·학년)
1. **후보**: 선택한 전공에 연결된 자격증 (`major_qualifications`). 학부 이름을 고르면 소속 학과 연결을 합산
2. **응시 시점**: 학교 종류·학년·등급으로 지금 가능 / N학년부터 / 졸업·경력 후 / 개별 확인 / 제한 없음 판정
3. **점수**: 학과 연결(+30~40) · 응시 시점(0~+20) · 올해 남은 시험(+10) · 등급 적합(+5 / −20). 희망 직무·관심 분야는 쓰지 않음
4. **결과**: 점수순 최대 5개(`items`). 학과 연결이 적으면 그 수만큼. 학교 정보가 없는 회원은 `needs_school_info: true`(결과 없음, 조건 선택 안내). 학교 정보는 있는데 학과 연결이 없으면 `needs_school_info: false` + 빈 `items`(학과 연결 없음 안내)

**② 조건 선택 추천** (`recommend_by_condition`, 추천 페이지에서 고른 자격 구분·관심 분야·희망 직무)
* 점수: 희망 직무 +30 · 관심 분야 +20 · 남은 시험 +10 · 응시 조건 없음 +5, 학교·전공·학년 제외
* 동점이면 최근 연도 1차 실제 응시자 많은 순 → 종목코드
* 결과: 점수순 최대 5개(`items`), 후보가 적으면 그 수만큼. 없으면 빈 목록(조건 변경 안내)

**지금 주목받는 자격증**: `trends(limit=3)` — 최근 완결 연도 1차 응시자 순위 1~3위

자세한 입출력은 [docs/API_CONTRACT.md](docs/API_CONTRACT.md)에 있습니다.

---

## 🔧 활용

```bash
python run.py check                                  # 데이터 무결성 + 전체 테스트
python run.py recommend profile.json                 # 추천 결과 JSON (입력 형식: docs/API_CONTRACT.md 2장)
python run.py qualification 1320                     # 자격증 상세
python run.py options majors --school-id <학교 ID>   # 선택 목록
```

### 데이터를 다시 만들 때

데이터 생성 스크립트와 보정표(`build_service_data.py`, 학과명 보완 규칙·응시 조건 원본·국가전문 직무분야 매핑·자격증 소개 문구, 한 줄 소개 검토 목록)는 원천 프로젝트(Planner-DataSchool-v2)의 `tools/`·`docs/`에 있습니다. 원천 프로젝트에서 빌드한 `data/*.csv`와 `MANIFEST.json`만 이 폴더 `data/`로 복사한 뒤 확인합니다.

```bash
python run.py check
```

* 골든 결과(`tests/golden_recommendations.json`)가 달라지면 의도한 변경인지 확인한 뒤 갱신합니다
* 자격증 한 줄 소개를 고칠 때는 원천 프로젝트 `tools/qualification_texts.csv`의 `summary`만 바꾸고 다시 빌드합니다

---

## ⚠️ 알아둘 점

* 추천의 응시 시점은 **진로 안내**이며 개인 응시자격 확정이 아닙니다 (`personal_eligibility_verified: false`)
* 합격률은 **실제 응시자 기준**입니다 (접수자 아님)
* 자격증 한 줄 소개(`summary`)는 서비스가 작성한 문구입니다. 공단 수행직무 근거(`QNET_TEXT`, 389종)와 시험과목·직무분야 근거(`EXAM_INFO`, 224종)로 구분하며, `EXAM_INFO`는 원천 프로젝트의 `docs/qualification_summary_review.xlsx`로 검토합니다
* CSV는 엑셀에서 바로 열 수 있게 UTF-8 BOM으로 저장합니다

## MySQL 초기 적재

`docs/schema.sql`을 적용한 **빈 DB**에서 실행합니다. 이미 데이터가 있는 DB에 합치는 용도가 아닙니다. 적재는 한 트랜잭션으로 하며 MySQL 클라이언트의 `--force`를 끕니다. 생성되는 `service-data-ver12.sql`은 실행용 임시 파일이라 커밋하지 않습니다.

```powershell
mysql --login-path=cert_sniper_local --default-character-set=utf8mb4 --execute="source docs/schema.sql"
python etl/tools/load_service_data.py --output service-data-ver12.sql
mysql --login-path=cert_sniper_local --skip-force --default-character-set=utf8mb4 --execute="source service-data-ver12.sql"
```

`cert_sniper_local`은 로컬 MySQL 로그인 프로필 이름입니다. 생성 단계의 종료 코드가 0이면 적재합니다. CSV의 원천 ID는 DB에 넣지 않고 DB ID(1부터)로 바꿔 FK를 채웁니다. 관심 분야(23개)·희망 직무(50개)는 자격증 Master의 대·중직무분야로 채우고, 국가기술 등급(`qualifications.grade_level`)은 추천 계산과 같은 판정으로 넣습니다(국가전문은 NULL). 합격 통계는 2001년~최근 완결 연도만 넣고, 합격률(`pass_rate_pct`)은 생성 컬럼이라 넣지 않습니다. 사자성어 50개(`daily_idioms.csv`)도 같은 SQL로 들어갑니다.

## 트렌드와 시험 단계

트렌드 순위(지금 주목받는 자격증)만 종목별 **첫 시험**(필기 → 1차 → 단일 시험 순으로 한 단계) 실제 응시자 수를 씁니다(CF-02-08). 자격증 상세의 합격률·그래프·연도별 상세·시험일정은 필기·실기, 1차·2차·3차·면접, 단일 시험을 **모두** 보여 줍니다(`tests/test_service.py`의 `test_detail_keeps_every_exam_stage`). 백엔드는 트렌드 조회를 SQL 파일로 따로 두지 않고 `TrendService`의 리포지토리 쿼리로 구현합니다.
