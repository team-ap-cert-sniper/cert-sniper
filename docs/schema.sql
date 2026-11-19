-- 저격증 서비스 DB (jg_service) · DB 설계서 Ver.7 기준 · MySQL 8.0.16 이상
-- ERD 프로그램(ERDCloud·dbdiagram·draw.io·DBeaver 등)의 SQL 가져오기에 넣거나 MySQL에서 실행합니다.
-- 개수 제한·소유권 같은 업무 규칙은 서비스에서 검증하고, 여기에는 컬럼 제약만 둡니다.
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '회원 ID',
  email VARCHAR(254) NOT NULL COMMENT '이메일(아이디)',
  password_hash VARCHAR(100) NOT NULL COMMENT '비밀번호 해시',
  name VARCHAR(20) NOT NULL COMMENT '이름',
  nickname VARCHAR(10) NOT NULL COMMENT '닉네임',
  role VARCHAR(10) NOT NULL DEFAULT 'USER' COMMENT '권한',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '계정 상태',
  university_id BIGINT NULL COMMENT '대학 ID',
  college_id BIGINT NULL COMMENT '학부(단과대학) ID',
  major_id BIGINT NULL COMMENT '학과 ID',
  grade VARCHAR(15) NULL COMMENT '학년 (학교 정보 없음이면 NULL)',
  confirmed_hidden_count INT NOT NULL DEFAULT 0 COMMENT '누적 숨김 횟수',
  suspended_until DATETIME NULL COMMENT '정지 종료 일시',
  withdrawal_requested_at DATETIME NULL COMMENT '탈퇴 신청 일시',
  last_login_at DATETIME NULL COMMENT '최종 로그인 일시',
  withdrawn_at DATETIME NULL COMMENT '탈퇴 확정 일시',
  purge_due_at DATETIME NULL COMMENT '연관 데이터 삭제 예정일시',
  purged_at DATETIME NULL COMMENT '연관 데이터 삭제 완료일시',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_users_email (email),
  UNIQUE KEY uq_users_nickname (nickname),
  KEY idx_users_status (status),
  KEY idx_users_purge_due_at (purge_due_at),
  CONSTRAINT fk_users_university_id FOREIGN KEY (university_id) REFERENCES universities (id),
  CONSTRAINT fk_users_college_id FOREIGN KEY (college_id) REFERENCES colleges (id),
  CONSTRAINT fk_users_major_id FOREIGN KEY (major_id) REFERENCES majors (id),
  CHECK (role IN ('USER', 'ADMIN')),
  CHECK (status IN ('ACTIVE', 'SUSPENDED', 'WITHDRAWAL_PENDING', 'WITHDRAWN')),
  CHECK (grade IN ('1', '2', '3', '4', '5', '6', 'GRADUATED', 'NOT_ENROLLED')),
  CHECK ((university_id IS NULL AND major_id IS NULL AND grade IS NULL) OR (university_id IS NOT NULL AND grade IS NOT NULL)),
  CHECK (confirmed_hidden_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='회원';

CREATE TABLE user_tokens (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '토큰 ID',
  user_id BIGINT NOT NULL COMMENT '회원 ID',
  token_hash CHAR(64) NOT NULL COMMENT '토큰 해시',
  token_family CHAR(36) NOT NULL COMMENT '토큰 계열 ID',
  expires_at DATETIME NOT NULL COMMENT '만료 일시',
  last_used_at DATETIME NULL COMMENT '최종 재발급 사용 일시',
  user_agent VARCHAR(255) NULL COMMENT '접속 환경',
  client_ip VARCHAR(45) NULL COMMENT '접속 IP',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '발급 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_user_tokens_token_hash (token_hash),
  KEY idx_user_tokens_token_family (token_family),
  KEY idx_user_tokens_expires_at (expires_at),
  CONSTRAINT fk_user_tokens_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Refresh Token 화이트리스트';

CREATE TABLE email_verifications (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '인증 ID',
  user_id BIGINT NULL COMMENT '회원 ID',
  email VARCHAR(254) NOT NULL COMMENT '발송 이메일',
  purpose VARCHAR(20) NOT NULL COMMENT '용도',
  code_hash CHAR(64) NOT NULL COMMENT '인증코드 해시',
  code_salt CHAR(32) NOT NULL COMMENT '해시 salt',
  expires_at DATETIME NOT NULL COMMENT '코드 만료 일시',
  attempt_count TINYINT NOT NULL DEFAULT 0 COMMENT '검증 시도 횟수',
  send_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '발송 상태',
  sent_at DATETIME NULL COMMENT '발송 성공 일시',
  send_error_code VARCHAR(30) NULL COMMENT '발송 실패 코드',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '상태',
  verified_at DATETIME NULL COMMENT '인증 성공 일시',
  verified_token_hash CHAR(64) NULL COMMENT '인증 완료 토큰 해시',
  verified_token_expires_at DATETIME NULL COMMENT '인증 완료 토큰 만료',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  KEY idx_email_verifications_email_purpose_status (email, purpose, status),
  KEY idx_email_verifications_email (email),
  UNIQUE KEY uq_email_verifications_verified_token_hash (verified_token_hash),
  CONSTRAINT fk_email_verifications_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CHECK (purpose IN ('SIGNUP', 'PASSWORD_RESET', 'PASSWORD_CHANGE')),
  CHECK (attempt_count BETWEEN 0 AND 5),
  CHECK (send_status IN ('PENDING', 'SENT', 'FAILED')),
  CHECK (status IN ('PENDING', 'VERIFIED', 'USED', 'EXPIRED', 'LOCKED', 'INVALIDATED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='이메일 인증코드';

CREATE TABLE email_auth_limits (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  email VARCHAR(254) NOT NULL COMMENT '이메일',
  purpose VARCHAR(20) NOT NULL COMMENT '용도',
  failure_count SMALLINT NOT NULL DEFAULT 0 COMMENT '누적 실패 횟수',
  window_started_at DATETIME NOT NULL COMMENT '집계 시작 일시',
  restricted_until DATETIME NULL COMMENT '제한 종료 일시',
  last_failed_at DATETIME NULL COMMENT '최근 실패 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_email_auth_limits_email_purpose (email, purpose),
  KEY idx_email_auth_limits_restricted_until (restricted_until),
  CHECK (purpose IN ('SIGNUP', 'PASSWORD_RESET', 'PASSWORD_CHANGE')),
  CHECK (failure_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='이메일 인증 누적 실패 제한';

CREATE TABLE user_certificates (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  user_id BIGINT NOT NULL COMMENT '회원 ID',
  jmcd VARCHAR(20) NULL COMMENT '자격 종목코드',
  cert_name VARCHAR(100) NULL COMMENT '자격증명(직접 입력)',
  acquired_date DATE NULL COMMENT '취득일',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_user_certificates_user_id_jmcd (user_id, jmcd),
  CONSTRAINT fk_user_certificates_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_user_certificates_jmcd FOREIGN KEY (jmcd) REFERENCES qualifications (jmcd)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='보유 자격증';

CREATE TABLE account_status_histories (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '이력 ID',
  user_id BIGINT NOT NULL COMMENT '회원 ID',
  change_type VARCHAR(30) NOT NULL COMMENT '변경 유형',
  previous_status VARCHAR(20) NOT NULL COMMENT '변경 전 상태',
  new_status VARCHAR(20) NOT NULL COMMENT '변경 후 상태',
  reason VARCHAR(500) NULL COMMENT '사유',
  starts_at DATETIME NULL COMMENT '정지 시작 일시',
  ends_at DATETIME NULL COMMENT '정지 종료 일시',
  admin_id BIGINT NULL COMMENT '처리 관리자',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '기록 일시',
  PRIMARY KEY (id),
  KEY idx_account_status_histories_user_id_created_at (user_id, created_at),
  CONSTRAINT fk_account_status_histories_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_account_status_histories_admin_id FOREIGN KEY (admin_id) REFERENCES users (id),
  CHECK (change_type IN ('SUSPEND', 'UNSUSPEND', 'AUTO_UNSUSPEND', 'WITHDRAW_REQUEST', 'WITHDRAW_CANCEL', 'WITHDRAW_CONFIRM'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='계정 상태 이력';

CREATE TABLE universities (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '대학 ID',
  source VARCHAR(20) NOT NULL DEFAULT 'KCUE' COMMENT '원천',
  source_univ_code VARCHAR(20) NOT NULL COMMENT '원천 대학 코드',
  name VARCHAR(100) NOT NULL COMMENT '대학명',
  campus_name VARCHAR(50) NULL COMMENT '캠퍼스명',
  school_type VARCHAR(20) NULL COMMENT '학교 유형',
  region VARCHAR(30) NULL COMMENT '소재 지역',
  is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '운영 여부',
  source_base_date DATE NULL COMMENT '원천 기준일',
  synced_at DATETIME NULL COMMENT '최종 적재 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_universities_source_source_univ_code (source, source_univ_code),
  KEY idx_universities_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='대학';

CREATE TABLE colleges (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '단과대학 ID',
  university_id BIGINT NOT NULL COMMENT '대학 ID',
  source_college_code VARCHAR(30) NULL COMMENT '원천 학부 코드',
  name VARCHAR(100) NOT NULL COMMENT '학부·단과대학명',
  is_list_item TINYINT(1) NOT NULL DEFAULT 0 COMMENT '전공 목록 노출 여부',
  is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '운영 여부',
  source_base_date DATE NULL COMMENT '원천 기준일',
  synced_at DATETIME NULL COMMENT '최종 적재 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_colleges_university_id_name (university_id, name),
  CONSTRAINT fk_colleges_university_id FOREIGN KEY (university_id) REFERENCES universities (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='학부·단과대학';

CREATE TABLE majors (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '학과 ID',
  university_id BIGINT NOT NULL COMMENT '대학 ID',
  college_id BIGINT NULL COMMENT '소속 학부 ID',
  source_major_code VARCHAR(30) NOT NULL COMMENT '원천 학과 코드',
  name VARCHAR(100) NOT NULL COMMENT '학과·전공명',
  parent_name VARCHAR(100) NULL COMMENT '소속 표시명',
  degree_course VARCHAR(20) NULL COMMENT '과정',
  academic_field VARCHAR(30) NULL COMMENT '학문 계열(대계열)',
  discipline VARCHAR(30) NULL COMMENT '학과명 보완 계열',
  aggregate_college_id BIGINT NULL COMMENT '합산 대상 학부 ID',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '학과 상태',
  successor_major_id BIGINT NULL COMMENT '개편 후 학과 ID',
  source_base_date DATE NULL COMMENT '원천 기준일',
  synced_at DATETIME NULL COMMENT '최종 적재 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_majors_university_id_source_major_code (university_id, source_major_code),
  KEY idx_majors_name (name),
  CONSTRAINT fk_majors_university_id FOREIGN KEY (university_id) REFERENCES universities (id),
  CONSTRAINT fk_majors_college_id FOREIGN KEY (college_id) REFERENCES colleges (id),
  CONSTRAINT fk_majors_aggregate_college_id FOREIGN KEY (aggregate_college_id) REFERENCES colleges (id),
  CONSTRAINT fk_majors_successor_major_id FOREIGN KEY (successor_major_id) REFERENCES majors (id),
  CHECK (status IN ('ACTIVE', 'CLOSED', 'REORGANIZED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='학과·전공';

CREATE TABLE major_aliases (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  major_id BIGINT NOT NULL COMMENT '학과 ID',
  alias VARCHAR(100) NOT NULL COMMENT '별칭',
  alias_type VARCHAR(20) NOT NULL COMMENT '별칭 유형',
  PRIMARY KEY (id),
  UNIQUE KEY uq_major_aliases_major_id_alias (major_id, alias),
  KEY idx_major_aliases_alias (alias),
  CONSTRAINT fk_major_aliases_major_id FOREIGN KEY (major_id) REFERENCES majors (id) ON DELETE CASCADE,
  CHECK (alias_type IN ('OLD_NAME', 'ABBR', 'SOURCE_NAME', 'MERGED_CODE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='학과 별칭';

CREATE TABLE job_categories (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '직무 ID',
  code VARCHAR(30) NOT NULL COMMENT '직무 코드',
  name VARCHAR(50) NOT NULL COMMENT '직무명',
  interest_field_id BIGINT NOT NULL COMMENT '관심 분야 ID',
  sort_order SMALLINT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
  is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '사용 여부',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_job_categories_code (code),
  CONSTRAINT fk_job_categories_interest_field_id FOREIGN KEY (interest_field_id) REFERENCES interest_fields (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='희망 직무';

CREATE TABLE interest_fields (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '관심 분야 ID',
  code VARCHAR(30) NOT NULL COMMENT '관심 분야 코드',
  name VARCHAR(50) NOT NULL COMMENT '관심 분야명',
  sort_order SMALLINT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
  is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '사용 여부',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_interest_fields_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='관심 분야';

CREATE TABLE major_qualification_links (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  major_id BIGINT NOT NULL COMMENT '학과 ID',
  jmcd VARCHAR(20) NOT NULL COMMENT '자격 종목코드',
  link_rank SMALLINT NOT NULL COMMENT '연결 순서',
  basis VARCHAR(30) NOT NULL COMMENT '연결 근거 유형',
  link_basis VARCHAR(100) NULL COMMENT '근거 설명',
  recommendation_type VARCHAR(30) NULL COMMENT '추천 유형',
  source_base_date DATE NULL COMMENT '원천 기준일',
  synced_at DATETIME NULL COMMENT '최종 적재 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_major_qualification_links_major_id_jmcd (major_id, jmcd),
  CONSTRAINT fk_major_qualification_links_major_id FOREIGN KEY (major_id) REFERENCES majors (id) ON DELETE CASCADE,
  CONSTRAINT fk_major_qualification_links_jmcd FOREIGN KEY (jmcd) REFERENCES qualifications (jmcd)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='학과–자격증 연결';

CREATE TABLE recommendation_weight_policies (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '정책 ID',
  version VARCHAR(20) NOT NULL COMMENT '정책 버전',
  policy_type VARCHAR(20) NOT NULL DEFAULT 'MAJOR' COMMENT '정책 종류',
  score_rules JSON NOT NULL COMMENT '점수표',
  tie_breaker VARCHAR(100) NOT NULL DEFAULT 'SCORE_DESC,LINK_RANK_ASC,JMCD_ASC' COMMENT '동점 정렬 규칙',
  is_active TINYINT(1) NOT NULL DEFAULT 0 COMMENT '활성 여부',
  active_flag VARCHAR(20) GENERATED ALWAYS AS (IF(is_active = 1, policy_type, NULL)) STORED COMMENT '종류별 활성 1건 확인용',
  description VARCHAR(255) NULL COMMENT '설명',
  created_by BIGINT NULL COMMENT '등록 관리자',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_recommendation_weight_policies_policy_type_version (policy_type, version),
  UNIQUE KEY uq_recommendation_weight_policies_active_flag (active_flag),
  CHECK (policy_type IN ('MAJOR', 'CONDITION')),
  CONSTRAINT fk_recommendation_weight_policies_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='추천 점수 정책';

CREATE TABLE recommendation_conditions (
  user_id BIGINT NOT NULL COMMENT '회원 ID',
  university_id BIGINT NULL COMMENT '대학 ID',
  major_id BIGINT NULL COMMENT '학과·전공 ID',
  college_id BIGINT NULL COMMENT '학부 ID',
  grade VARCHAR(20) NULL COMMENT '학년',
  qualification_type VARCHAR(20) NULL COMMENT '자격구분',
  interest_field_codes JSON NULL COMMENT '관심 분야 코드 목록',
  job_category_codes JSON NULL COMMENT '희망 직무 코드 목록',
  confirmed_at DATETIME NULL COMMENT '조건 반영 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (user_id),
  CONSTRAINT fk_recommendation_conditions_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_recommendation_conditions_university_id FOREIGN KEY (university_id) REFERENCES universities (id),
  CONSTRAINT fk_recommendation_conditions_major_id FOREIGN KEY (major_id) REFERENCES majors (id),
  CONSTRAINT fk_recommendation_conditions_college_id FOREIGN KEY (college_id) REFERENCES colleges (id),
  CHECK (qualification_type IN ('TECHNICAL', 'PROFESSIONAL', 'NULL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='추천 조건 (현재 미사용, 팀 확인 전까지 유지)';

CREATE TABLE qualifications (
  jmcd VARCHAR(20) NOT NULL COMMENT '자격 종목코드',
  jmfldnm VARCHAR(100) NOT NULL COMMENT '자격 종목명',
  eng_name VARCHAR(200) NULL COMMENT '영문명',
  qualgbcd VARCHAR(5) NULL COMMENT '자격구분 코드',
  qualgbnm VARCHAR(30) NULL COMMENT '자격구분명',
  seriescd VARCHAR(5) NULL COMMENT '계열·등급 코드',
  seriesnm VARCHAR(30) NULL COMMENT '계열·등급명',
  obligfldcd VARCHAR(10) NULL COMMENT '대직무분야 코드',
  obligfldnm VARCHAR(50) NULL COMMENT '대직무분야명',
  mdobligfldcd VARCHAR(10) NULL COMMENT '중직무분야 코드',
  mdobligfldnm VARCHAR(50) NULL COMMENT '중직무분야명',
  related_ministry VARCHAR(50) NULL COMMENT '관련부처',
  summary VARCHAR(200) NULL COMMENT '한 줄 소개',
  summary_basis VARCHAR(20) NULL COMMENT '소개 근거',
  overview TEXT NULL COMMENT '개요',
  job_description TEXT NULL COMMENT '수행직무',
  career_outlook TEXT NULL COMMENT '진로 및 전망',
  conducting_agency VARCHAR(200) NULL COMMENT '시행처',
  related_majors TEXT NULL COMMENT '관련학과',
  exam_method TEXT NULL COMMENT '검정방법',
  exam_subjects TEXT NULL COMMENT '시험과목',
  exam_standard TEXT NULL COMMENT '합격 기준',
  eligibility TEXT NULL COMMENT '응시자격',
  eligibility_required CHAR(1) NULL COMMENT '응시 조건 여부',
  eligibility_summary VARCHAR(100) NULL COMMENT '응시 조건 안내',
  eligibility_rule_key VARCHAR(30) NULL COMMENT '응시 조건 키',
  fee_written INT NULL COMMENT '필기 수수료',
  fee_practical INT NULL COMMENT '실기 수수료',
  fee_note VARCHAR(300) NULL COMMENT '수수료 안내',
  schedule_type VARCHAR(10) NOT NULL DEFAULT '정기' COMMENT '시험 형태',
  is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '서비스 노출 여부',
  source_base_date DATE NULL COMMENT '원천 기준일',
  etl_synced_at DATETIME NULL COMMENT '최종 ETL 적재 일시',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (jmcd),
  KEY idx_qualifications_jmfldnm (jmfldnm),
  KEY idx_qualifications_obligfldcd (obligfldcd),
  KEY idx_qualifications_mdobligfldcd (mdobligfldcd),
  KEY idx_qualifications_is_active (is_active),
  KEY idx_qualifications_eligibility_required (eligibility_required),
  KEY idx_qualifications_eligibility_rule_key (eligibility_rule_key),
  CHECK (summary_basis IN ('QNET_TEXT', 'EXAM_INFO')),
  CHECK (eligibility_required IN ('Y', 'N')),
  CHECK (schedule_type IN ('정기', '상시'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='자격증 Master';

CREATE TABLE qualification_subject_items (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '과목 ID',
  jmcd VARCHAR(20) NOT NULL COMMENT '자격 종목코드',
  exam_stage VARCHAR(10) NOT NULL DEFAULT 'WRITTEN' COMMENT '시험 단계',
  seq TINYINT NOT NULL COMMENT '과목 순서',
  subject_name VARCHAR(100) NOT NULL COMMENT '과목명',
  parse_rule_version VARCHAR(20) NULL COMMENT '분해 규칙 버전',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_qualification_subject_items_jmcd_exam_stage_seq (jmcd, exam_stage, seq),
  CONSTRAINT fk_qualification_subject_items_jmcd FOREIGN KEY (jmcd) REFERENCES qualifications (jmcd),
  CHECK (exam_stage IN ('WRITTEN', 'PRACTICAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='분해된 시험과목';

CREATE TABLE qualification_statistics_yearly (
  jmcd VARCHAR(20) NOT NULL COMMENT '자격 종목코드',
  year SMALLINT NOT NULL COMMENT '기준 연도',
  exam_stage VARCHAR(10) NOT NULL COMMENT '시험 단계',
  applicants INT NULL COMMENT '접수자 수',
  examinees INT NULL COMMENT '응시자 수',
  passers INT NULL COMMENT '합격자 수',
  pass_rate_pct DECIMAL(5,2) NULL COMMENT '합격률(%)',
  data_source VARCHAR(30) NOT NULL COMMENT '통계 출처',
  source_url VARCHAR(500) NULL COMMENT '출처 URL',
  synced_at DATETIME NULL COMMENT '최종 적재 일시',
  PRIMARY KEY (jmcd, year, exam_stage),
  CONSTRAINT fk_qualification_statistics_yearly_jmcd FOREIGN KEY (jmcd) REFERENCES qualifications (jmcd),
  CHECK (examinees >= 0),
  CHECK (passers >= 0),
  CHECK (pass_rate_pct BETWEEN 0 AND 100),
  CHECK (applicants >= examinees),
  CHECK (data_source IN ('QNET_PAGE', 'QNET_OFFICIAL', 'QNET_STATS_API'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='연도·단계별 합격 통계';

CREATE TABLE qualification_exam_schedules (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '일정 ID',
  jmcd VARCHAR(20) NOT NULL COMMENT '자격 종목코드',
  exam_year SMALLINT NOT NULL COMMENT '시행 연도',
  exam_round VARCHAR(20) NULL COMMENT '회차',
  exam_stage VARCHAR(10) NOT NULL COMMENT '시험 단계',
  registration_start_date DATE NULL COMMENT '정기 접수 시작일',
  registration_end_date DATE NULL COMMENT '정기 접수 종료일',
  vacancy_registration_start_date DATE NULL COMMENT '빈자리 접수 시작일',
  vacancy_registration_end_date DATE NULL COMMENT '빈자리 접수 종료일',
  exam_start_date DATE NOT NULL COMMENT '시험 시작일',
  exam_end_date DATE NOT NULL COMMENT '시험 종료일',
  pass_announcement_date DATE NULL COMMENT '합격 발표일',
  source_type VARCHAR(30) NOT NULL COMMENT '일정 출처',
  source_url VARCHAR(500) NULL COMMENT '출처 URL',
  source_note VARCHAR(100) NULL COMMENT '출처 메모',
  source_base_date DATE NULL COMMENT '원천 기준일',
  synced_at DATETIME NULL COMMENT '최종 적재 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_qualification_exam_schedules_jmcd_exam_stage_exam_start_date_ (jmcd, exam_stage, exam_start_date, exam_end_date),
  KEY idx_qualification_exam_schedules_exam_start_date (exam_start_date),
  CONSTRAINT fk_qualification_exam_schedules_jmcd FOREIGN KEY (jmcd) REFERENCES qualifications (jmcd),
  CHECK (source_type IN ('OFFICIAL_ROUNDS', 'QNET_SCHEDULE_API'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='시험일정';

CREATE TABLE qualification_eligibility_rules (
  rule_key VARCHAR(30) NOT NULL COMMENT '응시 조건 키',
  seq TINYINT NOT NULL COMMENT '조건 순서',
  condition_type VARCHAR(10) NOT NULL COMMENT '조건 구분',
  condition_text VARCHAR(300) NOT NULL COMMENT '조건 내용',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (rule_key, seq),
  CHECK (condition_type IN ('학력', '경력', '자격', '훈련', '기타'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='응시 조건 목록';

CREATE TABLE favorites (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '찜 ID',
  user_id BIGINT NOT NULL COMMENT '회원 ID',
  jmcd VARCHAR(20) NOT NULL COMMENT '자격 종목코드',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '찜 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_favorites_user_id_jmcd (user_id, jmcd),
  CONSTRAINT fk_favorites_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_favorites_jmcd FOREIGN KEY (jmcd) REFERENCES qualifications (jmcd)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='관심 자격증';

CREATE TABLE target_exams (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '목표 자격증 ID',
  user_id BIGINT NOT NULL COMMENT '회원 ID',
  jmcd VARCHAR(20) NOT NULL COMMENT '자격 종목코드',
  exam_schedule_id BIGINT NULL COMMENT '선택한 시험 회차',
  exam_date DATE NULL COMMENT '목표 시험일',
  display_color CHAR(7) NOT NULL COMMENT '일정 색상',
  plan_source VARCHAR(10) NOT NULL DEFAULT 'MANUAL' COMMENT '일정 생성 방식',
  ai_generated_at DATETIME NULL COMMENT 'AI 플랜 생성 일시',
  ai_meta JSON NULL COMMENT 'AI 플랜 요약',
  sort_order TINYINT NOT NULL DEFAULT 0 COMMENT '표시 순서',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_target_exams_user_id_jmcd (user_id, jmcd),
  UNIQUE KEY uq_target_exams_user_id_display_color (user_id, display_color),
  CONSTRAINT fk_target_exams_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_target_exams_jmcd FOREIGN KEY (jmcd) REFERENCES qualifications (jmcd),
  CONSTRAINT fk_target_exams_exam_schedule_id FOREIGN KEY (exam_schedule_id) REFERENCES qualification_exam_schedules (id) ON DELETE SET NULL,
  CHECK (display_color REGEXP '^#[0-9A-Fa-f]{6}$'),
  CHECK (plan_source IN ('MANUAL', 'AI'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='목표 자격증';

CREATE TABLE planners (
  user_id BIGINT NOT NULL COMMENT '회원 ID',
  mode VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '화면 상태',
  mode_changed_at DATETIME NULL COMMENT '상태 전환 일시',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (user_id),
  CONSTRAINT fk_planners_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CHECK (mode IN ('AI_RENDER', 'MANUAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='플래너 다이어리';

CREATE TABLE personal_schedules (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '개인 일정 ID',
  user_id BIGINT NOT NULL COMMENT '회원 ID',
  name VARCHAR(20) NOT NULL COMMENT '일정 이름',
  display_color CHAR(7) NOT NULL COMMENT '일정 색상',
  sort_order TINYINT NOT NULL DEFAULT 0 COMMENT '표시 순서',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  CONSTRAINT fk_personal_schedules_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CHECK (display_color REGEXP '^#[0-9A-Fa-f]{6}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='개인 일정';

CREATE TABLE planner_bars (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '일정바 ID',
  user_id BIGINT NOT NULL COMMENT '회원 ID',
  target_exam_id BIGINT NULL COMMENT '목표 자격증 ID',
  personal_schedule_id BIGINT NULL COMMENT '개인 일정 ID',
  title VARCHAR(30) NOT NULL COMMENT '일정명',
  start_date DATE NOT NULL COMMENT '시작일',
  end_date DATE NOT NULL COMMENT '종료일',
  is_single_day TINYINT(1) NOT NULL DEFAULT 0 COMMENT '당일 일정 여부',
  source VARCHAR(10) NOT NULL DEFAULT 'MANUAL' COMMENT '생성 출처',
  sort_order SMALLINT NOT NULL DEFAULT 0 COMMENT '표시 순서',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  KEY idx_planner_bars_user_id_start_date (user_id, start_date),
  CONSTRAINT fk_planner_bars_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_planner_bars_target_exam_id FOREIGN KEY (target_exam_id) REFERENCES target_exams (id) ON DELETE CASCADE,
  CONSTRAINT fk_planner_bars_personal_schedule_id FOREIGN KEY (personal_schedule_id) REFERENCES personal_schedules (id) ON DELETE CASCADE,
  CHECK (end_date >= start_date),
  CHECK (source IN ('MANUAL', 'AI')),
  CHECK (is_single_day = 0 OR start_date = end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='일정바';

CREATE TABLE planner_milestones (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '항목 ID',
  bar_id BIGINT NOT NULL COMMENT '일정바 ID',
  user_id BIGINT NOT NULL COMMENT '회원 ID',
  point_date DATE NOT NULL COMMENT '날짜',
  content VARCHAR(100) NOT NULL COMMENT '내용',
  is_rest TINYINT(1) NOT NULL DEFAULT 0 COMMENT '휴식 여부',
  source VARCHAR(10) NOT NULL DEFAULT 'MANUAL' COMMENT '생성 출처',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  KEY idx_planner_milestones_user_id_point_date (user_id, point_date),
  CONSTRAINT fk_planner_milestones_bar_id FOREIGN KEY (bar_id) REFERENCES planner_bars (id) ON DELETE CASCADE,
  CONSTRAINT fk_planner_milestones_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CHECK (source IN ('MANUAL', 'AI'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='세부 일정·마일스톤';

CREATE TABLE memos (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '메모 ID',
  user_id BIGINT NOT NULL COMMENT '회원 ID',
  target_exam_id BIGINT NOT NULL COMMENT '목표 자격증 ID',
  content_text VARCHAR(500) NOT NULL COMMENT '메모 텍스트',
  content_html TEXT NOT NULL COMMENT '서식 본문',
  version INT NOT NULL DEFAULT 0 COMMENT '버전',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부',
  deleted_at DATETIME NULL COMMENT '삭제 일시',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_memos_target_exam_id (target_exam_id),
  CONSTRAINT fk_memos_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_memos_target_exam_id FOREIGN KEY (target_exam_id) REFERENCES target_exams (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='자격증별 메모';

CREATE TABLE pass_stories (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '게시글 ID',
  user_id BIGINT NOT NULL COMMENT '작성 회원 ID',
  jmcd VARCHAR(20) NOT NULL COMMENT '합격 자격 종목코드',
  title VARCHAR(100) NOT NULL COMMENT '제목',
  body VARCHAR(1500) NOT NULL COMMENT '상세 후기·실전 팁',
  examinee_base VARCHAR(20) NULL COMMENT '응시자 베이스',
  prep_period VARCHAR(20) NULL COMMENT '준비 기간',
  attempt_band VARCHAR(10) NULL COMMENT '응시 횟수',
  textbook_name VARCHAR(50) NULL COMMENT '사용 교재명',
  pass_score DECIMAL(6,2) NULL COMMENT '합격 점수',
  pass_stage VARCHAR(10) NULL COMMENT '합격 구분',
  exam_date DATE NULL COMMENT '시험 응시일',
  status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' COMMENT '게시 상태',
  hidden_reason VARCHAR(20) NULL COMMENT '숨김 사유',
  hidden_at DATETIME NULL COMMENT '숨김 일시',
  hidden_confirmed_by BIGINT NULL COMMENT '숨김 확정 관리자',
  hidden_confirmed_at DATETIME NULL COMMENT '숨김 확정 일시',
  deleted_at DATETIME NULL COMMENT '삭제 일시',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  KEY idx_pass_stories_user_id_status_created_at (user_id, status, created_at),
  KEY idx_pass_stories_status_created_at (status, created_at),
  CONSTRAINT fk_pass_stories_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_pass_stories_jmcd FOREIGN KEY (jmcd) REFERENCES qualifications (jmcd),
  CONSTRAINT fk_pass_stories_hidden_confirmed_by FOREIGN KEY (hidden_confirmed_by) REFERENCES users (id),
  CHECK (examinee_base IN ('NO_BASE', 'BASIC', 'MAJOR', 'PRACTITIONER')),
  CHECK (prep_period IN ('UNDER_1M', 'M1_TO_3M', 'M3_TO_6M', 'OVER_6M')),
  CHECK (attempt_band IN ('1', '2', '3', '4_PLUS')),
  CHECK (pass_score >= 0),
  CHECK (status IN ('PUBLISHED', 'HIDDEN', 'REVIEW_PENDING', 'DELETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='합격수기';

CREATE TABLE pass_story_subject_difficulties (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  story_id BIGINT NOT NULL COMMENT '게시글 ID',
  subject_item_id BIGINT NULL COMMENT '시험과목 ID',
  subject_name VARCHAR(100) NOT NULL COMMENT '과목명',
  difficulty VARCHAR(10) NOT NULL COMMENT '체감 난이도',
  PRIMARY KEY (id),
  UNIQUE KEY uq_pass_story_subject_difficulties_story_id_subject_name (story_id, subject_name),
  CONSTRAINT fk_pass_story_subject_difficulties_story_id FOREIGN KEY (story_id) REFERENCES pass_stories (id) ON DELETE CASCADE,
  CONSTRAINT fk_pass_story_subject_difficulties_subject_item_id FOREIGN KEY (subject_item_id) REFERENCES qualification_subject_items (id) ON DELETE SET NULL,
  CHECK (difficulty IN ('EASY', 'NORMAL', 'HARD'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='과목별 체감 난이도';

CREATE TABLE pass_story_reports (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '신고 ID',
  story_id BIGINT NOT NULL COMMENT '게시글 ID',
  reporter_id BIGINT NOT NULL COMMENT '신고 회원 ID',
  report_type VARCHAR(20) NOT NULL COMMENT '신고 사유',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '처리 상태',
  reviewed_by BIGINT NULL COMMENT '검토 관리자',
  reviewed_at DATETIME NULL COMMENT '검토 일시',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '신고 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_pass_story_reports_story_id_reporter_id (story_id, reporter_id),
  KEY idx_pass_story_reports_status_created_at (status, created_at),
  CONSTRAINT fk_pass_story_reports_story_id FOREIGN KEY (story_id) REFERENCES pass_stories (id),
  CONSTRAINT fk_pass_story_reports_reporter_id FOREIGN KEY (reporter_id) REFERENCES users (id),
  CONSTRAINT fk_pass_story_reports_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users (id),
  CHECK (report_type IN ('IRRELEVANT', 'ABUSE', 'PRIVACY', 'AD', 'ETC')),
  CHECK (status IN ('PENDING', 'CONFIRMED', 'DISMISSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='합격수기 신고';

CREATE TABLE pass_story_moderation_logs (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '이력 ID',
  story_id BIGINT NOT NULL COMMENT '게시글 ID',
  action VARCHAR(20) NOT NULL COMMENT '조치',
  actor_id BIGINT NOT NULL COMMENT '처리자 ID',
  actor_role VARCHAR(10) NOT NULL COMMENT '처리자 구분',
  report_id BIGINT NULL COMMENT '관련 신고 ID',
  reason_code VARCHAR(20) NULL COMMENT '사유 코드',
  reason_text VARCHAR(500) NULL COMMENT '관리자 메모',
  previous_status VARCHAR(20) NULL COMMENT '변경 전 상태',
  new_status VARCHAR(20) NOT NULL COMMENT '변경 후 상태',
  content_snapshot JSON NULL COMMENT '내용 스냅샷',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '기록 일시',
  PRIMARY KEY (id),
  KEY idx_pass_story_moderation_logs_story_id_created_at (story_id, created_at),
  CONSTRAINT fk_pass_story_moderation_logs_story_id FOREIGN KEY (story_id) REFERENCES pass_stories (id),
  CONSTRAINT fk_pass_story_moderation_logs_actor_id FOREIGN KEY (actor_id) REFERENCES users (id),
  CONSTRAINT fk_pass_story_moderation_logs_report_id FOREIGN KEY (report_id) REFERENCES pass_story_reports (id),
  CHECK (action IN ('REPORT_HIDE', 'CONFIRM_HIDE', 'DISMISS', 'RESUBMIT', 'UNHIDE', 'DELETE', 'AUTHOR_EDIT', 'AUTHOR_DELETE')),
  CHECK (actor_role IN ('ADMIN', 'AUTHOR', 'REPORTER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='게시글 상태 변경 이력';

CREATE TABLE user_ranks (
  user_id BIGINT NOT NULL COMMENT '회원 ID',
  valid_story_count INT NOT NULL DEFAULT 0 COMMENT '유효 합격수기 수',
  distinct_cert_count INT NOT NULL DEFAULT 0 COMMENT '합격 자격증 수',
  auto_rank VARCHAR(20) NOT NULL DEFAULT 'TRAINEE' COMMENT '자동 산정 등급',
  override_rank VARCHAR(20) NULL COMMENT '보정 등급',
  override_active TINYINT(1) NOT NULL DEFAULT 0 COMMENT '보정 활성 여부',
  override_reason VARCHAR(500) NULL COMMENT '보정 사유',
  override_by BIGINT NULL COMMENT '보정 관리자',
  override_at DATETIME NULL COMMENT '보정 일시',
  effective_rank VARCHAR(20) GENERATED ALWAYS AS (IF(override_active=1, override_rank, auto_rank)) STORED COMMENT '최종 적용 등급',
  last_calculated_at DATETIME NULL COMMENT '최종 산정 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (user_id),
  CONSTRAINT fk_user_ranks_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_user_ranks_override_by FOREIGN KEY (override_by) REFERENCES users (id),
  CHECK (valid_story_count >= 0),
  CHECK (distinct_cert_count >= 0),
  CHECK (auto_rank IN ('TRAINEE', 'APPRENTICE', 'MARKSMAN', 'LEGEND'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='회원 저격 등급';

CREATE TABLE user_rank_histories (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '이력 ID',
  user_id BIGINT NOT NULL COMMENT '회원 ID',
  change_type VARCHAR(20) NOT NULL COMMENT '변경 유형',
  previous_auto_rank VARCHAR(20) NULL COMMENT '변경 전 자동 등급',
  new_auto_rank VARCHAR(20) NULL COMMENT '변경 후 자동 등급',
  previous_effective_rank VARCHAR(20) NOT NULL COMMENT '변경 전 적용 등급',
  new_effective_rank VARCHAR(20) NOT NULL COMMENT '변경 후 적용 등급',
  trigger_story_id BIGINT NULL COMMENT '원인 게시글 ID',
  reason VARCHAR(500) NULL COMMENT '사유',
  admin_id BIGINT NULL COMMENT '처리 관리자',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '기록 일시',
  PRIMARY KEY (id),
  KEY idx_user_rank_histories_user_id_created_at (user_id, created_at),
  CONSTRAINT fk_user_rank_histories_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_user_rank_histories_trigger_story_id FOREIGN KEY (trigger_story_id) REFERENCES pass_stories (id),
  CONSTRAINT fk_user_rank_histories_admin_id FOREIGN KEY (admin_id) REFERENCES users (id),
  CHECK (change_type IN ('AUTO', 'OVERRIDE', 'OVERRIDE_RELEASE', 'RECALCULATE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='등급 변경 이력';

CREATE TABLE rank_notifications (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '알림 ID',
  user_id BIGINT NOT NULL COMMENT '회원 ID',
  story_id BIGINT NOT NULL COMMENT '등록 게시글 ID',
  previous_rank VARCHAR(20) NOT NULL COMMENT '이전 적용 등급',
  new_rank VARCHAR(20) NOT NULL COMMENT '새 적용 등급',
  shown_at DATETIME NULL COMMENT '표시 일시',
  dismissed_at DATETIME NULL COMMENT '확인 일시',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  PRIMARY KEY (id),
  KEY idx_rank_notifications_user_id_dismissed_at (user_id, dismissed_at),
  UNIQUE KEY uq_rank_notifications_user_id_story_id (user_id, story_id),
  CONSTRAINT fk_rank_notifications_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_rank_notifications_story_id FOREIGN KEY (story_id) REFERENCES pass_stories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='승급 알림';

CREATE TABLE common_codes (
  group_code VARCHAR(30) NOT NULL COMMENT '코드 그룹',
  code VARCHAR(30) NOT NULL COMMENT '코드',
  code_name VARCHAR(100) NOT NULL COMMENT '코드명',
  sort_order SMALLINT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
  is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '사용 여부',
  description VARCHAR(255) NULL COMMENT '설명',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (group_code, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='공통 코드';

CREATE TABLE notices (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '공지 ID',
  author_id BIGINT NOT NULL COMMENT '작성 관리자',
  notice_type VARCHAR(20) NOT NULL DEFAULT 'GENERAL' COMMENT '공지 유형',
  title VARCHAR(100) NOT NULL COMMENT '제목',
  content TEXT NOT NULL COMMENT '본문',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부',
  deleted_at DATETIME NULL COMMENT '삭제 일시',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  KEY idx_notices_is_deleted_created_at (is_deleted, created_at),
  CONSTRAINT fk_notices_author_id FOREIGN KEY (author_id) REFERENCES users (id),
  CHECK (notice_type IN ('GENERAL', 'MAINTENANCE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='공지사항';

CREATE TABLE faqs (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'FAQ ID',
  author_id BIGINT NULL COMMENT '작성 관리자',
  question VARCHAR(200) NOT NULL COMMENT '질문',
  answer TEXT NOT NULL COMMENT '답변',
  sort_order SMALLINT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부',
  deleted_at DATETIME NULL COMMENT '삭제 일시',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  CONSTRAINT fk_faqs_author_id FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='FAQ';

CREATE TABLE inquiries (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '문의 ID',
  user_id BIGINT NOT NULL COMMENT '작성 회원 ID',
  inquiry_type_code VARCHAR(30) NOT NULL COMMENT '문의 유형',
  title VARCHAR(50) NOT NULL COMMENT '문의 제목',
  content VARCHAR(1000) NOT NULL COMMENT '문의 내용',
  status VARCHAR(20) NOT NULL DEFAULT 'WAITING' COMMENT '처리 상태',
  answered_at DATETIME NULL COMMENT '답변 완료 일시',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '접수 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  KEY idx_inquiries_user_id_created_at (user_id, created_at),
  KEY idx_inquiries_status_created_at (status, created_at),
  CONSTRAINT fk_inquiries_user_id FOREIGN KEY (user_id) REFERENCES users (id),
  CHECK (status IN ('WAITING', 'ANSWERED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='1:1 문의';

CREATE TABLE inquiry_answers (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '답변 ID',
  inquiry_id BIGINT NOT NULL COMMENT '문의 ID',
  admin_id BIGINT NOT NULL COMMENT '답변 관리자',
  content VARCHAR(1000) NOT NULL COMMENT '답변 내용',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '답변 일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (id),
  UNIQUE KEY uq_inquiry_answers_inquiry_id (inquiry_id),
  CONSTRAINT fk_inquiry_answers_inquiry_id FOREIGN KEY (inquiry_id) REFERENCES inquiries (id),
  CONSTRAINT fk_inquiry_answers_admin_id FOREIGN KEY (admin_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='문의 답변';

CREATE TABLE admin_audit_logs (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '로그 ID',
  admin_id BIGINT NOT NULL COMMENT '관리자 ID',
  target_user_id BIGINT NULL COMMENT '대상 회원 ID',
  action VARCHAR(40) NOT NULL COMMENT '작업',
  target_table VARCHAR(64) NOT NULL COMMENT '대상 테이블',
  target_id VARCHAR(50) NOT NULL COMMENT '대상 키',
  before_json JSON NULL COMMENT '변경 전 값',
  after_json JSON NULL COMMENT '변경 후 값',
  reason VARCHAR(500) NULL COMMENT '처리 사유',
  request_id CHAR(36) NULL COMMENT '요청 ID',
  client_ip VARCHAR(45) NULL COMMENT '접속 IP',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '기록 일시',
  PRIMARY KEY (id),
  KEY idx_admin_audit_logs_target_user_id_created_at (target_user_id, created_at),
  KEY idx_admin_audit_logs_target_table_target_id (target_table, target_id),
  CONSTRAINT fk_admin_audit_logs_admin_id FOREIGN KEY (admin_id) REFERENCES users (id),
  CONSTRAINT fk_admin_audit_logs_target_user_id FOREIGN KEY (target_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='관리자 변경 이력';

SET FOREIGN_KEY_CHECKS = 1;
