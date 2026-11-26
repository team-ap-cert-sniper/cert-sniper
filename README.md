# 🎯 저격증 (학과 기반 국가자격증 추천 및 학습 관리 플랫폼)

**저격증**은 학교·전공·학년과 관심 분야·희망 직무로 국가자격증을 추천하고, 자격증 정보·시험 일정·학습 계획·합격 후기를 한 곳에서 관리하는 웹 서비스입니다.

---

## 👨‍👩‍👧‍👦 팀 소개 (TEAM AP)

| 이름 | 담당 화면 | 백엔드 패키지 | 비고 |
|---|---|---|---|
| **박준성(PM)** | 플래너 다이어리(AI · 직접 쓰기) VIEW-07·08 | `targetexam` `planner` `memo` `aiplanner` | Git관리, GitHub 저장소·백엔드 초기 설정, AI 학습 |
| **이은아(PL)** | 자격증 추천 VIEW-05, 자격증 상세 VIEW-06 | `option` `university` `job` `recommendation` `qualification` `examschedule` `global` | 개발 뼈대(백엔드 공통·API 틀, 프론트 공통·라우터), 자동 검사·코드 규칙, DB 관리자(공용 DB 구조·데이터), 추천 알고리즘·ETL(`etl/`) |
| **유다연** | 초기 화면(메인·온보딩) VIEW-01, 회원가입·로그인·비밀번호 재설정 VIEW-02~04 | `auth` | 기능 흐름, 디자인, 공통 헤더·푸터·팝업 |
| **최기완** | 저격왕 합격수기 VIEW-09·10, 마이페이지 VIEW-11 | `user` `favorite` `passstory` `report` `rank` | 기능 흐름, 디자인 |
| **황정규** | 고객센터 VIEW-12·13, 관리자 VIEW-14~19 | `support` `admin` | 기능 흐름, 디자인 |


---

## 🚀 프로젝트 목표 및 필요성

* **자격증 정보 통합**: Q-Net·공공데이터에 흩어진 자격증 정보, 시험 일정, 합격률을 한 화면에서 확인
* **전공 맞춤 추천**: 같은 계열이라도 학교마다 다른 학과 이름을 학과 단위로 연결해, 내 전공에 실제로 맞는 자격증을 추천
* **응시 확인**: 학년·학제와 Q-Net 응시자격 기준으로 "지금 가능 / N학년부터 / 졸업 후"를 함께 표시
* **추천에서 합격까지**: 추천받은 자격증으로 학습 계획을 세우고, 합격 후기를 공유하는 흐름을 하나로 연결

---

## ✨ 핵심 기능
### 1. 학과 기반 자격증 추천

* **두 가지 추천**: ① **전공 맞춤 추천**(처음 진입·새로고침, 회원정보의 대학교·전공·학년) ② **조건 선택 추천**(자격 구분·관심 분야·희망 직무만, 학교·전공·학년 제외) — 결과 최대 5개(후보가 적으면 그 수만큼), 조건은 저장하지 않음
* **점수 기반 추천**: 전공 맞춤은 학과 연결·학년별 응시 시점·올해 남은 시험·학년에 맞는 시작 등급, 조건 선택은 희망 직무·관심 분야·남은 시험·응시 조건 없음을 점수로 더해 순위를 매기고 **항목별 추천 이유**를 함께 제공
* **추천 카드**: 한 줄 소개 · 최근 합격률 · 다음 접수 기간 · 가장 가까운 시험일, 카드에서 찜 · 상세보기 · AI 플래너 생성하기

### 2. 자격증 정보

* **자격증 상세**: 영문명, 관련부처, 수수료, 한 줄 소개, 개요, 수행직무, 시험과목, 합격기준
* **응시 조건**: 자격증마다 응시 조건 필요 / 없음 / 확인 필요와 조건 목록(학력·경력·자격·훈련)
* **자격증 검색·탐색**: 종목명 검색 → 결과 팝업(자격구분·등급), 자격구분 → 직무분야 → 분류 → 종목 탐색 (상세는 조회 전용)
* **시험 일정**: 회차별 정기·빈자리 접수·시험·발표, 상태(접수 전·접수 중·빈자리 접수·마감·시험 기간 중·종료)와 D-n
* **합격률**: 2001년부터 연도·단계별 접수자·응시자·합격자·응시율·합격률 (막대·꺾은선 차트, 연도 선택)
* **트렌드**: 응시자가 많은 자격증 순위(국가기술·국가전문 분리)와 5년 증감

### 3. 학습 관리

* **플래너 다이어리**: 목표 자격증별 일정바·당일 일정(마일스톤)·시험일·D-Day, 개인 일정 (합계 최대 6개)
* **AI 학습 플래너**: 7단계 설문 → Qwen2.5가 학습 플랜 생성 → 서버 검증 후 플래너에 저장
* **자격증별 메모 · 관심 자격증 · 오늘의 일정**: 메모 500자, 찜한 자격증은 마이페이지에서 확인, 메인에서 오늘 일정 확인

### 4. 저격왕 커뮤니티

* **합격수기**: 준비 기간, 응시 횟수, 사용 교재 공유
* **저격 등급**: 합격수기 활동에 따른 등급과 승급 알림
* **신고·제재**: 신고 즉시 숨김, 관리자 확정 횟수에 따른 단계별 정지

### 5. 고객센터 · 관리자

* **고객센터**: 공지사항 · FAQ · 1:1 문의
* **관리자**: 대시보드, 회원 정정·정지, 공지·문의·신고 관리

---

## 🛠 기술 스택 (Tech Stack)

### **Backend**

* **Framework**: Java 21 · Spring Boot 3.5.16 · Gradle 8.14.3 (래퍼 포함)
* **Database**: MySQL 8.0 (팀 공용 DB 1개)
* **JPA/ORM**: Spring Data JPA
* **DB 변경 관리**: DB 관리자만 공용 DB에 반영
* **인증**: Spring Security · JWT (jjwt 0.12.6)
* **API 문서**: springdoc-openapi 2.8.9 (Swagger UI)
* **코드 생성**: Lombok
* **메일**: JavaMailSender (Google SMTP)
* **AI**: Qwen2.5 API (설문 기반 학습 플랜 생성)

### **Frontend**

* **Runtime**: Node.js 24 LTS (`frontend/.nvmrc`)
* **Library**: React 19 · Vite 8
* **Routing / HTTP**: React Router · Axios
* **Chart**: Chart.js, react-chartjs-2
* **코드 규칙**: ESLint · Prettier

### **Data Pipeline**

* **Language**: Python (표준 라이브러리)
* **원천 데이터**: 한국산업인력공단 Q-Net 자격정보·시험일정·합격률, 한국대학교육협의회 대학·학과 정보, 공공데이터포털

### **Collaboration & Tools**

* **IDE**: VS Code 또는 Antigravity. 코드 모양은 `.editorconfig`·Prettier·ESLint·PR 자동 검사로 맞춥니다 (VS Code 확장: Extension Pack for Java, Spring Boot Extension Pack, ESLint, Prettier, EditorConfig)
* **VCS**: Git · GitHub (Organization)
* **CI**: GitHub Actions (PR 자동 검사)
* **Docs / Design**: Notion · Figma

---

## 🏗 시스템 구조 (Architecture)

React ↔ Spring Boot **REST API (JSON, `/api/v1`)**

* **Client**: 화면별 페이지와 API 호출 모듈로 사용자 요청 처리
* **Server**: 기능별 패키지(Controller → Service → Repository)로 비즈니스 로직 수행
* **Storage**: MySQL 공용 DB (31개 테이블, [ERD](docs/ERD.md))
* **Data Pipeline**: Python으로 공공데이터를 수집·정제해 CSV로 만들고 DB에 적재. 추천 로직의 기준 구현과 테스트도 함께 보관

---

## 📁 폴더 구조

```
📁 cert-sniper/
├─ 📁 backend/    Spring Boot 서버
├─ 📁 frontend/   React 웹
├─ 📁 etl/        공공데이터 정리 결과 + 추천 로직 기준 구현
├─ 📁 docs/       ERD · API 명세 · 기능 추적표 · 플로우차트
├─ 📄 .env.example
└─ 📁 .github/    PR·이슈 템플릿, CODEOWNERS, 자동 검사(workflows)
```

| 구조 문서 | 내용 |
|---|---|
| 🖥 [backend](backend/README.md) | 패키지별 담당, 파일 전체 트리(뼈대 있음), 뼈대 사용법, 패키지끼리 부르는 곳, 이름 규칙 |
| 🎨 [frontend](frontend/README.md) | 화면별 담당, 파일 전체 트리(뼈대 있음), 화면 경로·화면 이동 전체, 이름 규칙 |
| 🔄 [etl](etl/README.md) | 서비스용 데이터(CSV 10종, 사자성어 포함), 추천·자격증 로직, CSV–테이블 정의 |
| 🗂 [ERD](docs/ERD.md) | 테이블 관계도 (Mermaid) |
| 📡 [API 명세](docs/API_CONTRACT.md) | 추천·자격증 API 경로·입출력·화면 표시 순서 |
| 🔎 [기능 추적표](docs/TRACEABILITY.md) | 기능 ID → 담당 → 화면 → 프론트 위치 → API → 백엔드 패키지 → 테이블 |
| 🧭 [플로우차트](docs/FLOWCHART.md) | 전체 + 파트별 화면 흐름 (Mermaid) |

---

## 🗄 공용 DB

DB는 팀 공용 DB 하나를 같이 씁니다. 각자 DB를 설치하거나 실행하지 않습니다. 백엔드·프론트·Swagger만 각자 노트북에서 실행합니다.

1. 레포 루트에서 `cp .env.example .env`
2. `.env`의 DB·JWT·메일 값을 카톡으로 받아 채웁니다. 백엔드도 이 `.env`를 그대로 읽습니다

공용 DB에는 서비스 데이터(`etl/data` CSV 10종: 공공데이터 9종 + 사자성어 50개)와 관리자 계정 1개(`euna@certsniper.admin`, 비밀번호는 팀 안에서 공유)가 들어 있습니다.

* 접속 정보(주소·포트·계정·비밀번호)는 레포·문서·PR에 쓰지 않습니다
* 내가 만든 테스트 데이터도 팀원 모두에게 보입니다. 다른 팀원이 만든 데이터는 수정·삭제하지 않습니다
* Workbench 등으로 공용 DB에 직접 접속해 데이터·구조를 바꾸지 않습니다. DB 구조·기준 데이터 변경은 DB 관리자(이은아)만 하고, 필요하면 이슈로 요청합니다

## ▶ 백엔드 · 프론트 실행

| | 처음 한 번 | 실행 | 주소 |
|---|---|---|---|
| 백엔드 | JDK 21 설치 (설정은 위 `.env` 하나로 끝) | `cd backend` → `./gradlew bootRun` (Windows `gradlew.bat bootRun`) | http://localhost:8080/swagger-ui.html |
| 프론트 | Node.js 24 설치 (`node -v`로 확인) | `cd frontend` → `npm install` → `npm run dev` | http://localhost:5173 (`/api`는 8080으로 전달) |

* 터미널 대신 에디터 버튼으로도 실행됩니다: 백엔드는 `CertSniperApplication.java`의 ▶ Run, 프론트는 탐색기 아래 **NPM 스크립트**의 `dev` ▶ (`npm install`은 처음 한 번)
* 화면은 백엔드·프론트를 둘 다 켠 상태에서 http://localhost:5173 으로 봅니다

## 🌿 브랜치 전략

```
main        ← 최종(배포). 이은아·박준성만 병합
└─ develop  ← 통합. PR로만 병합
   └─ feature/{파트}/{작업내용}
```

| 종류 | 형식 | 예시 |
|---|---|---|
| 기능 개발 | `feature/{파트}/{작업내용}` | `feature/recommend/score` |
| 버그 수정 | `fix/{파트}/{작업내용}` | `fix/recommend/sort-order` |
| 문서 | `docs/{작업내용}` | `docs/update-readme` |

* **파트**: 맡은 기능 (아래 표)

  | 파트 | 기능 | 담당 |
  |---|---|---|
  | `main` | 초기 메인 화면 | 유다연 |
  | `auth` | 회원가입·로그인·비밀번호 재설정 | 유다연 |
  | `recommend` | 자격증 추천 | 이은아 |
  | `qualification` | 자격증 상세·합격률·트렌드·시험일정 | 이은아 |
  | `planner` | 플래너 다이어리·AI 플래너·메모·목표 자격증 | 박준성 |
  | `passstory` | 저격왕 합격수기·신고·저격 등급 | 최기완 |
  | `user` | 마이페이지·찜(관심 자격증) | 최기완 |
  | `support` | 고객센터 | 황정규 |
  | `admin` | 관리자 | 황정규 |
  | `common` | 공통 설정·뼈대 | 이은아 |

* **작업내용**: 영어 소문자로 짧게, 단어 사이는 `-` (예: `login-form`, `score`, `pass-rate-chart`)
* 브랜치 이름에 **front / backend / 사람 이름은 넣지 않습니다.** 한 브랜치에서 화면과 API를 같이 작업합니다

---

## 🔁 협업 규칙

### 작업 순서

1. **이슈 생성**: 작업 하나에 이슈 하나, 담당자 지정 (기능 명세서 번호를 알면 제목에 같이 적기)
2. **브랜치 생성**: `develop`에서 분기
   ```bash
   git switch develop
   git pull
   git switch -c feature/recommend/score
   ```
3. **작업·커밋**: 화면·API·DB를 한 브랜치에서
4. **PR**: `feature/...` → `develop`, 템플릿 작성, `Closes #이슈번호`
5. **리뷰·병합**: 승인 1명 이상 + 자동 검사 통과 후 병합, 병합한 브랜치 삭제
6. **배포**: 이은아·박준성이 `develop` → `main` 병합

### 커밋 메시지

```
{종류}: {내용}
예) feat: 추천 점수 항목별 이유 반환
```

| 종류 | 뜻 |
|---|---|
| `feat` | 새 기능 |
| `fix` | 버그 수정 |
| `refactor` | 동작 변화 없는 코드 정리 |
| `docs` | 문서 |
| `test` | 테스트 |
| `chore` | 설정·빌드 |

### 브랜치 보호

| 브랜치 | 규칙 |
|---|---|
| `main` | 직접 push 금지, PR 필수, 이은아·박준성만 병합, CODEOWNERS 승인 필수 |
| `develop` | 직접 push 금지, PR 필수, 승인 1명 이상, CODEOWNERS 승인 필수, 자동 검사 통과 |
| `feature/*` | 담당자 자유롭게 push |

* 승인자는 `.github/CODEOWNERS`에 있습니다: 모든 PR은 이은아·박준성 중 1명, ETL·`.env.example`·백엔드 `global` 변경은 이은아
* 자동 검사: PR을 올리면 GitHub Actions(`.github/workflows/ci.yml`)가 backend 컴파일, frontend 코드 모양·규칙(`npm run format` · `npm run lint`)·빌드, etl 테스트를 실행합니다. 실패하면 병합하지 않습니다
* API 키·비밀번호는 **절대 커밋하지 않습니다** (`.env`, `application-local.yml`은 `.gitignore` 대상)
