-- DB설계서-Cert-Sniper (테이블 31개 · FK 46개)

-- ===== 회원·인증 =====

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '회원 ID',
  `email` varchar(254) NOT NULL COMMENT '이메일(아이디)',
  `password` varchar(100) NOT NULL COMMENT '비밀번호(BCrypt 해시)',
  `name` varchar(4) NOT NULL COMMENT '이름',
  `nickname` varchar(10) NOT NULL COMMENT '닉네임',
  `role` varchar(10) NOT NULL DEFAULT 'USER' COMMENT '권한',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '계정 상태',
  `university_id` bigint NULL COMMENT '대학 ID',
  `college_id` bigint NULL COMMENT '학부(단과대학) ID',
  `major_id` bigint NULL COMMENT '학과 ID',
  `grade` varchar(15) NULL COMMENT '학년',
  `confirmed_hidden_count` int NOT NULL DEFAULT 0 COMMENT '누적 숨김 횟수',
  `suspended_until` datetime NULL COMMENT '정지 종료 일시',
  `withdrawal_requested_at` datetime NULL COMMENT '탈퇴 신청 일시',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입 일시',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_users_1` (`email`),
  UNIQUE KEY `uq_users_2` (`nickname`),
  KEY `idx_users_1` (`status`, `suspended_until`),
  KEY `idx_users_2` (`status`, `withdrawal_requested_at`),
  CONSTRAINT `ck_users_1` CHECK (role IN ('USER','ADMIN')),
  CONSTRAINT `ck_users_2` CHECK (status IN ('ACTIVE','SUSPENDED','WITHDRAWAL_PENDING','WITHDRAWN')),
  CONSTRAINT `ck_users_3` CHECK (CHAR_LENGTH(name) BETWEEN 2 AND 4),
  CONSTRAINT `ck_users_4` CHECK (CHAR_LENGTH(nickname) BETWEEN 2 AND 10),
  CONSTRAINT `ck_users_5` CHECK (grade IS NULL OR grade IN ('1','2','3','4','5','6','GRADUATED','NOT_ENROLLED')),
  CONSTRAINT `ck_users_6` CHECK ((university_id IS NULL AND college_id IS NULL AND major_id IS NULL AND grade IS NULL) OR (university_id IS NOT NULL AND grade IS NOT NULL AND (college_id IS NOT NULL OR major_id IS NOT NULL))),
  CONSTRAINT `ck_users_7` CHECK (confirmed_hidden_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='회원';

CREATE TABLE `user_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '토큰 ID',
  `user_id` bigint NOT NULL COMMENT '회원 ID',
  `token_hash` char(64) NOT NULL COMMENT '토큰 해시',
  `token_family` char(36) NOT NULL COMMENT '토큰 계열 ID',
  `expires_at` datetime NOT NULL COMMENT '만료 일시',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_tokens_1` (`token_hash`),
  KEY `idx_user_tokens_1` (`token_family`),
  KEY `idx_user_tokens_2` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='로그인 유지 토큰';

CREATE TABLE `user_certificates` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '보유 자격증 ID',
  `user_id` bigint NOT NULL COMMENT '회원 ID',
  `jmcd` varchar(20) NULL COMMENT '자격 종목코드',
  `cert_name` varchar(100) NULL COMMENT '자격증명(직접 입력)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_certificates_1` (`user_id`, `jmcd`),
  UNIQUE KEY `uq_user_certificates_2` (`user_id`, `cert_name`),
  CONSTRAINT `ck_user_certificates_1` CHECK (jmcd IS NOT NULL OR cert_name IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='보유 자격증';

CREATE TABLE `account_status_histories` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '이력 ID',
  `user_id` bigint NOT NULL COMMENT '회원 ID',
  `change_type` varchar(30) NOT NULL COMMENT '변경 유형',
  `previous_status` varchar(20) NOT NULL COMMENT '변경 전 상태',
  `new_status` varchar(20) NOT NULL COMMENT '변경 후 상태',
  `reason` varchar(500) NULL COMMENT '사유',
  `ends_at` datetime NULL COMMENT '정지 종료 일시',
  `admin_id` bigint NULL COMMENT '처리 관리자',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '변경 일시',
  PRIMARY KEY (`id`),
  KEY `idx_account_status_histories_1` (`user_id`, `created_at`),
  CONSTRAINT `ck_account_status_histories_1` CHECK (change_type IN ('SUSPEND','UNSUSPEND','AUTO_UNSUSPEND','WITHDRAW_REQUEST','WITHDRAW_CANCEL','WITHDRAW_CONFIRM')),
  CONSTRAINT `ck_account_status_histories_2` CHECK (change_type NOT IN ('SUSPEND','UNSUSPEND') OR reason IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='계정 상태 이력';

-- ===== 대학·학과·직무·추천 =====

CREATE TABLE `universities` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '대학 ID',
  `name` varchar(100) NOT NULL COMMENT '대학명',
  `campus_name` varchar(50) NOT NULL COMMENT '캠퍼스명',
  `school_type` varchar(20) NOT NULL COMMENT '학교 유형',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_universities_1` (`name`, `campus_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='대학';

CREATE TABLE `colleges` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '단과대학 ID',
  `university_id` bigint NOT NULL COMMENT '대학 ID',
  `name` varchar(100) NOT NULL COMMENT '학부·단과대학명',
  `is_list_item` tinyint(1) NOT NULL DEFAULT 0 COMMENT '학부 전체 항목',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_colleges_1` (`university_id`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='학부·단과대학';

CREATE TABLE `majors` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '학과 ID',
  `university_id` bigint NOT NULL COMMENT '대학 ID',
  `college_id` bigint NULL COMMENT '소속 학부 ID',
  `name` varchar(100) NOT NULL COMMENT '학과·전공명',
  `parent_name` varchar(100) NULL COMMENT '소속 표시명',
  `aggregate_college_id` bigint NULL COMMENT '합산 대상 학부 ID',
  PRIMARY KEY (`id`),
  KEY `idx_majors_1` (`university_id`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='학과·전공';

CREATE TABLE `job_categories` (
  `code` varchar(10) NOT NULL COMMENT '직무 코드',
  `name` varchar(50) NOT NULL COMMENT '직무명',
  `interest_field_code` varchar(10) NOT NULL COMMENT '관심 분야 코드',
  PRIMARY KEY (`code`),
  KEY `idx_job_categories_1` (`interest_field_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='희망 직무';

CREATE TABLE `interest_fields` (
  `code` varchar(10) NOT NULL COMMENT '관심 분야 코드',
  `name` varchar(50) NOT NULL COMMENT '관심 분야명',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='관심 분야';

CREATE TABLE `major_qualification_links` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '연결 ID',
  `major_id` bigint NOT NULL COMMENT '학과 ID',
  `jmcd` varchar(20) NOT NULL COMMENT '자격 종목코드',
  `link_rank` smallint NOT NULL COMMENT '연결 순서',
  `basis` varchar(30) NOT NULL COMMENT '연결 근거 유형',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_major_qualification_links_1` (`major_id`, `jmcd`),
  KEY `idx_major_qualification_links_1` (`jmcd`),
  CONSTRAINT `ck_major_qualification_links_1` CHECK (basis IN ('existing_direct_links','recommended','same_subfield'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='학과–자격증 연결';

-- ===== 자격증 =====

CREATE TABLE `qualifications` (
  `jmcd` varchar(20) NOT NULL COMMENT '자격 종목코드',
  `jmfldnm` varchar(100) NOT NULL COMMENT '자격 종목명',
  `eng_name` varchar(200) NULL COMMENT '영문명',
  `qualgbcd` varchar(5) NULL COMMENT '자격구분 코드',
  `grade_level` varchar(10) NULL COMMENT '등급',
  `obligfldcd` varchar(10) NULL COMMENT '관심 분야 코드',
  `mdobligfldcd` varchar(10) NULL COMMENT '희망 직무 코드',
  `related_ministry` varchar(50) NULL COMMENT '관련부처',
  `summary` varchar(200) NULL COMMENT '한 줄 소개',
  `summary_basis` varchar(20) NULL COMMENT '소개 근거',
  `overview` text NULL COMMENT '개요',
  `job_description` text NULL COMMENT '수행직무',
  `career_outlook` text NULL COMMENT '진로 및 전망',
  `conducting_agency` varchar(200) NULL COMMENT '시행처',
  `related_majors` text NULL COMMENT '관련학과',
  `exam_method` text NULL COMMENT '검정방법',
  `exam_subjects` text NULL COMMENT '시험과목',
  `exam_standard` text NULL COMMENT '합격 기준',
  `eligibility` text NULL COMMENT '응시자격',
  `eligibility_required` char(1) NULL COMMENT '응시 조건 여부',
  `eligibility_summary` varchar(100) NULL COMMENT '응시 조건 안내',
  `eligibility_rule_key` varchar(30) NULL COMMENT '응시 조건 키',
  `fee_written` int NULL COMMENT '필기 수수료',
  `fee_practical` int NULL COMMENT '실기 수수료',
  `fee_note` varchar(300) NULL COMMENT '수수료 안내',
  `schedule_type` varchar(10) NOT NULL DEFAULT '정기' COMMENT '시험 형태',
  PRIMARY KEY (`jmcd`),
  CONSTRAINT `ck_qualifications_1` CHECK (qualgbcd IN ('T','S')),
  CONSTRAINT `ck_qualifications_2` CHECK (summary_basis IS NULL OR summary_basis IN ('QNET_TEXT','EXAM_INFO')),
  CONSTRAINT `ck_qualifications_3` CHECK (eligibility_required IS NULL OR eligibility_required IN ('Y','N')),
  CONSTRAINT `ck_qualifications_4` CHECK (schedule_type IN ('정기','상시')),
  CONSTRAINT `ck_qualifications_5` CHECK (grade_level IS NULL OR grade_level IN ('기능사','산업기사','기사','기능장','기술사','서비스')),
  CONSTRAINT `ck_qualifications_6` CHECK ((qualgbcd = 'T' AND grade_level IS NOT NULL) OR (qualgbcd = 'S' AND grade_level IS NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='자격증 Master';

CREATE TABLE `qualification_statistics_yearly` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '통계 ID',
  `jmcd` varchar(20) NOT NULL COMMENT '자격 종목코드',
  `year` smallint NOT NULL COMMENT '기준 연도',
  `exam_stage` varchar(10) NOT NULL COMMENT '시험 단계',
  `applicants` int NULL COMMENT '접수자 수',
  `examinees` int NULL COMMENT '응시자 수',
  `passers` int NULL COMMENT '합격자 수',
  `pass_rate_pct` decimal(5,2) GENERATED ALWAYS AS (IF(examinees > 0 AND passers IS NOT NULL, ROUND(passers * 100 / examinees, 2), NULL)) STORED COMMENT '합격률(%)',
  `data_source` varchar(30) NOT NULL COMMENT '통계 출처',
  `source_url` varchar(500) NULL COMMENT '출처 URL',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_qualification_statistics_yearly_1` (`jmcd`, `year`, `exam_stage`),
  KEY `idx_qualification_statistics_yearly_1` (`year`, `exam_stage`, `examinees`),
  CONSTRAINT `ck_qualification_statistics_yearly_1` CHECK (year >= 2001),
  CONSTRAINT `ck_qualification_statistics_yearly_2` CHECK (exam_stage IN ('필기','실기','1차','2차','3차','면접','시험')),
  CONSTRAINT `ck_qualification_statistics_yearly_3` CHECK (examinees IS NULL OR examinees >= 0),
  CONSTRAINT `ck_qualification_statistics_yearly_4` CHECK (applicants IS NULL OR examinees IS NULL OR applicants >= examinees),
  CONSTRAINT `ck_qualification_statistics_yearly_5` CHECK (passers IS NULL OR (passers >= 0 AND (examinees IS NULL OR passers <= examinees))),
  CONSTRAINT `ck_qualification_statistics_yearly_6` CHECK (pass_rate_pct IS NULL OR pass_rate_pct BETWEEN 0 AND 100),
  CONSTRAINT `ck_qualification_statistics_yearly_7` CHECK (data_source IN ('QNET_PAGE','QNET_OFFICIAL','QNET_STATS_API'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='연도·단계별 합격 통계';

CREATE TABLE `qualification_exam_schedules` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '일정 ID',
  `jmcd` varchar(20) NOT NULL COMMENT '자격 종목코드',
  `exam_round` varchar(20) NULL COMMENT '회차',
  `exam_stage` varchar(10) NOT NULL COMMENT '시험 단계',
  `registration_start_date` date NULL COMMENT '정기 접수 시작일',
  `registration_end_date` date NULL COMMENT '정기 접수 종료일',
  `vacancy_registration_start_date` date NULL COMMENT '빈자리 접수 시작일',
  `vacancy_registration_end_date` date NULL COMMENT '빈자리 접수 종료일',
  `exam_start_date` date NOT NULL COMMENT '시험 시작일',
  `exam_end_date` date NOT NULL COMMENT '시험 종료일',
  `pass_announcement_date` date NULL COMMENT '합격 발표일',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_qualification_exam_schedules_1` (`jmcd`, `exam_stage`, `exam_start_date`, `exam_end_date`),
  KEY `idx_qualification_exam_schedules_1` (`exam_start_date`),
  CONSTRAINT `ck_qualification_exam_schedules_1` CHECK (exam_stage IN ('필기','실기','1차','2차','3차','면접','시험')),
  CONSTRAINT `ck_qualification_exam_schedules_2` CHECK (exam_end_date >= exam_start_date),
  CONSTRAINT `ck_qualification_exam_schedules_3` CHECK (registration_end_date IS NULL OR registration_start_date IS NULL OR registration_end_date >= registration_start_date),
  CONSTRAINT `ck_qualification_exam_schedules_4` CHECK (vacancy_registration_end_date IS NULL OR vacancy_registration_start_date IS NULL OR vacancy_registration_end_date >= vacancy_registration_start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='시험일정';

CREATE TABLE `qualification_eligibility_rules` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '조건 ID',
  `rule_key` varchar(30) NOT NULL COMMENT '응시 조건 키',
  `seq` tinyint NOT NULL COMMENT '조건 순서',
  `condition_type` varchar(10) NOT NULL COMMENT '조건 구분',
  `condition_text` varchar(300) NOT NULL COMMENT '조건 내용',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_qualification_eligibility_rules_1` (`rule_key`, `seq`),
  CONSTRAINT `ck_qualification_eligibility_rules_1` CHECK (condition_type IN ('학력','경력','자격','훈련','기타'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='응시 조건 목록';

-- ===== 찜·플래너·메모 =====

CREATE TABLE `favorites` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '찜 ID',
  `user_id` bigint NOT NULL COMMENT '회원 ID',
  `jmcd` varchar(20) NOT NULL COMMENT '자격 종목코드',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '찜 일시',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_favorites_1` (`user_id`, `jmcd`),
  KEY `idx_favorites_1` (`user_id`, `created_at`),
  KEY `idx_favorites_2` (`jmcd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='관심 자격증';

CREATE TABLE `target_exams` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '목표 자격증 ID',
  `user_id` bigint NOT NULL COMMENT '회원 ID',
  `jmcd` varchar(20) NOT NULL COMMENT '자격 종목코드',
  `exam_schedule_id` bigint NULL COMMENT '선택한 시험 회차',
  `exam_date` date NULL COMMENT '목표 시험일',
  `display_color` char(7) NOT NULL COMMENT '일정 색상',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_target_exams_1` (`user_id`, `jmcd`),
  UNIQUE KEY `uq_target_exams_2` (`user_id`, `display_color`),
  CONSTRAINT `ck_target_exams_1` CHECK (display_color REGEXP '^#[0-9A-Fa-f]{6}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='목표 자격증';

CREATE TABLE `planners` (
  `user_id` bigint NOT NULL COMMENT '회원 ID',
  `mode` varchar(20) NOT NULL DEFAULT 'MANUAL' COMMENT '화면 상태',
  PRIMARY KEY (`user_id`),
  CONSTRAINT `ck_planners_1` CHECK (mode IN ('AI_RENDER','MANUAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='플래너 다이어리';

CREATE TABLE `personal_schedules` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '개인 일정 ID',
  `user_id` bigint NOT NULL COMMENT '회원 ID',
  `name` varchar(20) NOT NULL COMMENT '일정 이름',
  `display_color` char(7) NOT NULL COMMENT '일정 색상',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
  PRIMARY KEY (`id`),
  KEY `idx_personal_schedules_1` (`user_id`),
  CONSTRAINT `ck_personal_schedules_1` CHECK (display_color REGEXP '^#[0-9A-Fa-f]{6}$'),
  CONSTRAINT `ck_personal_schedules_2` CHECK (CHAR_LENGTH(TRIM(name)) BETWEEN 1 AND 20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='개인 일정';

CREATE TABLE `planner_bars` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '일정바 ID',
  `user_id` bigint NOT NULL COMMENT '회원 ID',
  `target_exam_id` bigint NULL COMMENT '목표 자격증 ID',
  `personal_schedule_id` bigint NULL COMMENT '개인 일정 ID',
  `title` varchar(100) NOT NULL COMMENT '일정명',
  `start_date` date NOT NULL COMMENT '시작일',
  `end_date` date NOT NULL COMMENT '종료일',
  `is_single_day` tinyint(1) NOT NULL DEFAULT 0 COMMENT '당일 일정 여부',
  `source` varchar(10) NOT NULL DEFAULT 'MANUAL' COMMENT '생성 방식',
  PRIMARY KEY (`id`),
  KEY `idx_planner_bars_1` (`user_id`, `start_date`, `end_date`),
  CONSTRAINT `ck_planner_bars_1` CHECK ((target_exam_id IS NULL) <> (personal_schedule_id IS NULL)),
  CONSTRAINT `ck_planner_bars_2` CHECK (end_date >= start_date),
  CONSTRAINT `ck_planner_bars_3` CHECK (is_single_day = 0 OR start_date = end_date),
  CONSTRAINT `ck_planner_bars_4` CHECK (source IN ('MANUAL','AI'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='일정바';

CREATE TABLE `planner_milestones` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '세부 일정 ID',
  `bar_id` bigint NOT NULL COMMENT '일정바 ID',
  `user_id` bigint NOT NULL COMMENT '회원 ID',
  `point_date` date NOT NULL COMMENT '날짜',
  `content` varchar(100) NOT NULL COMMENT '내용',
  `is_rest` tinyint(1) NOT NULL DEFAULT 0 COMMENT '휴식 여부',
  `source` varchar(10) NOT NULL DEFAULT 'MANUAL' COMMENT '생성 방식',
  PRIMARY KEY (`id`),
  KEY `idx_planner_milestones_1` (`user_id`, `point_date`),
  CONSTRAINT `ck_planner_milestones_1` CHECK (source IN ('MANUAL','AI'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='세부 일정·마일스톤';

CREATE TABLE `memos` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '메모 ID',
  `target_exam_id` bigint NOT NULL COMMENT '목표 자격증 ID',
  `content_text` varchar(500) NOT NULL COMMENT '메모 텍스트',
  `content_html` text NOT NULL COMMENT '서식 본문',
  `version` int NOT NULL DEFAULT 0 COMMENT '버전',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_memos_1` (`target_exam_id`),
  CONSTRAINT `ck_memos_1` CHECK (CHAR_LENGTH(content_text) BETWEEN 1 AND 500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='자격증별 메모';

CREATE TABLE `daily_idioms` (
  `idiom` varchar(10) NOT NULL COMMENT '사자성어',
  `hanja` varchar(10) NULL COMMENT '한자 표기',
  `meaning` varchar(200) NOT NULL COMMENT '뜻',
  `display_order` int NOT NULL COMMENT '순환 순서',
  PRIMARY KEY (`display_order`),
  UNIQUE KEY `uq_daily_idioms_1` (`idiom`),
  CONSTRAINT `ck_daily_idioms_1` CHECK (display_order >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='오늘의 사자성어';

-- ===== 저격왕 =====

CREATE TABLE `pass_stories` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '게시글 ID',
  `user_id` bigint NOT NULL COMMENT '작성 회원 ID',
  `jmcd` varchar(20) NOT NULL COMMENT '합격 자격 종목코드',
  `body` varchar(1500) NOT NULL COMMENT '상세 후기·실전 팁',
  `examinee_base` varchar(20) NULL COMMENT '응시자 베이스',
  `prep_period` varchar(20) NULL COMMENT '준비 기간',
  `attempt_band` varchar(10) NULL COMMENT '응시 횟수',
  `textbook_name` varchar(50) NULL COMMENT '사용 교재명',
  `pass_score` decimal(6,2) NULL COMMENT '합격 점수',
  `exam_date` date NULL COMMENT '시험 응시일',
  `status` varchar(20) NOT NULL DEFAULT 'PUBLISHED' COMMENT '게시 상태',
  `hidden_reason` varchar(20) NULL COMMENT '숨김 사유',
  `hidden_confirmed_at` datetime NULL COMMENT '숨김 확정 일시',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
  PRIMARY KEY (`id`),
  KEY `idx_pass_stories_1` (`user_id`, `status`, `created_at`),
  KEY `idx_pass_stories_2` (`status`, `created_at`),
  KEY `idx_pass_stories_3` (`jmcd`, `status`, `created_at`),
  CONSTRAINT `ck_pass_stories_1` CHECK (CHAR_LENGTH(body) BETWEEN 1 AND 1500),
  CONSTRAINT `ck_pass_stories_2` CHECK (examinee_base IS NULL OR examinee_base IN ('NO_BASE','BASIC','MAJOR','PRACTITIONER')),
  CONSTRAINT `ck_pass_stories_3` CHECK (prep_period IS NULL OR prep_period IN ('UNDER_1M','M1_TO_3M','M3_TO_6M','OVER_6M')),
  CONSTRAINT `ck_pass_stories_4` CHECK (attempt_band IS NULL OR attempt_band IN ('1','2','3','4_PLUS')),
  CONSTRAINT `ck_pass_stories_5` CHECK (pass_score IS NULL OR pass_score >= 0),
  CONSTRAINT `ck_pass_stories_6` CHECK (status IN ('PUBLISHED','HIDDEN','REVIEW_PENDING','DELETED')),
  CONSTRAINT `ck_pass_stories_7` CHECK (hidden_reason IS NULL OR hidden_reason IN ('IRRELEVANT','ABUSE','PRIVACY','AD','ETC'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='합격수기';

CREATE TABLE `pass_story_reports` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '신고 ID',
  `story_id` bigint NOT NULL COMMENT '게시글 ID',
  `reporter_id` bigint NOT NULL COMMENT '신고 회원 ID',
  `report_type` varchar(20) NOT NULL COMMENT '신고 사유',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '처리 상태',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '신고 일시',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pass_story_reports_1` (`story_id`, `reporter_id`),
  KEY `idx_pass_story_reports_1` (`status`, `created_at`),
  CONSTRAINT `ck_pass_story_reports_1` CHECK (report_type IN ('IRRELEVANT','ABUSE','PRIVACY','AD','ETC')),
  CONSTRAINT `ck_pass_story_reports_2` CHECK (status IN ('PENDING','CONFIRMED','DISMISSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='합격수기 신고';

CREATE TABLE `pass_story_moderation_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '이력 ID',
  `story_id` bigint NOT NULL COMMENT '게시글 ID',
  `action` varchar(20) NOT NULL COMMENT '조치',
  `actor_id` bigint NOT NULL COMMENT '처리자 ID',
  `actor_role` varchar(10) NOT NULL COMMENT '처리자 구분',
  `report_id` bigint NULL COMMENT '관련 신고 ID',
  `reason_code` varchar(20) NULL COMMENT '사유 코드',
  `reason_text` varchar(500) NULL COMMENT '관리자 메모',
  `previous_status` varchar(20) NULL COMMENT '변경 전 상태',
  `new_status` varchar(20) NOT NULL COMMENT '변경 후 상태',
  `previous_content` json NULL COMMENT '수정 전 내용',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '처리 일시',
  PRIMARY KEY (`id`),
  KEY `idx_pass_story_moderation_logs_1` (`story_id`, `created_at`),
  CONSTRAINT `ck_pass_story_moderation_logs_1` CHECK (action IN ('REPORT_HIDE','CONFIRM_HIDE','DISMISS','RESUBMIT','UNHIDE','DELETE','AUTHOR_EDIT','AUTHOR_DELETE')),
  CONSTRAINT `ck_pass_story_moderation_logs_2` CHECK (actor_role IN ('ADMIN','AUTHOR','REPORTER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='게시글 상태 변경 이력';

CREATE TABLE `user_ranks` (
  `user_id` bigint NOT NULL COMMENT '회원 ID',
  `valid_cert_count` int NOT NULL DEFAULT 0 COMMENT '유효 합격 자격증 수',
  `current_rank` varchar(20) NOT NULL DEFAULT 'TRAINEE' COMMENT '현재 등급',
  PRIMARY KEY (`user_id`),
  CONSTRAINT `ck_user_ranks_1` CHECK (valid_cert_count >= 0),
  CONSTRAINT `ck_user_ranks_2` CHECK (current_rank IN ('TRAINEE','APPRENTICE','MARKSMAN','LEGEND'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='회원 저격 등급';

-- ===== 고객센터·관리자 =====

CREATE TABLE `notices` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '공지 ID',
  `author_id` bigint NOT NULL COMMENT '작성 관리자',
  `notice_type` varchar(20) NOT NULL DEFAULT 'GENERAL' COMMENT '공지 유형',
  `title` varchar(100) NOT NULL COMMENT '제목',
  `content` text NOT NULL COMMENT '본문',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성 일시',
  PRIMARY KEY (`id`),
  KEY `idx_notices_1` (`is_deleted`, `created_at`),
  CONSTRAINT `ck_notices_1` CHECK (notice_type IN ('GENERAL','MAINTENANCE')),
  CONSTRAINT `ck_notices_2` CHECK (CHAR_LENGTH(title) BETWEEN 1 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='공지사항';

CREATE TABLE `faqs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'FAQ ID',
  `question` varchar(200) NOT NULL COMMENT '질문',
  `answer` text NOT NULL COMMENT '답변',
  `sort_order` smallint NOT NULL DEFAULT 0 COMMENT '정렬 순서',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='FAQ';

CREATE TABLE `inquiries` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '문의 ID',
  `user_id` bigint NOT NULL COMMENT '작성 회원 ID',
  `inquiry_type` varchar(20) NOT NULL COMMENT '문의 유형',
  `title` varchar(50) NOT NULL COMMENT '문의 제목',
  `content` varchar(1000) NOT NULL COMMENT '문의 내용',
  `status` varchar(20) NOT NULL DEFAULT 'WAITING' COMMENT '처리 상태',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '접수 일시',
  PRIMARY KEY (`id`),
  KEY `idx_inquiries_1` (`user_id`, `created_at`),
  KEY `idx_inquiries_2` (`status`, `created_at`),
  CONSTRAINT `ck_inquiries_1` CHECK (inquiry_type IN ('ACCOUNT','PLANNER','SUGGESTION','CERT_DATA','ETC')),
  CONSTRAINT `ck_inquiries_2` CHECK (status IN ('WAITING','ANSWERED')),
  CONSTRAINT `ck_inquiries_3` CHECK (CHAR_LENGTH(title) BETWEEN 1 AND 50),
  CONSTRAINT `ck_inquiries_4` CHECK (CHAR_LENGTH(content) BETWEEN 1 AND 1000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='1:1 문의';

CREATE TABLE `inquiry_answers` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '답변 ID',
  `inquiry_id` bigint NOT NULL COMMENT '문의 ID',
  `admin_id` bigint NOT NULL COMMENT '답변 관리자',
  `content` varchar(1000) NOT NULL COMMENT '답변 내용',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_inquiry_answers_1` (`inquiry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='문의 답변';

CREATE TABLE `admin_audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '로그 ID',
  `admin_id` bigint NOT NULL COMMENT '관리자 ID',
  `target_user_id` bigint NULL COMMENT '대상 회원 ID',
  `action` varchar(40) NOT NULL COMMENT '작업',
  `target_table` varchar(64) NOT NULL COMMENT '대상 테이블',
  `target_id` varchar(50) NOT NULL COMMENT '대상 키',
  `before_json` json NULL COMMENT '변경 전 값',
  `after_json` json NULL COMMENT '변경 후 값',
  `reason` varchar(500) NULL COMMENT '처리 사유',
  `request_id` char(36) NULL COMMENT '요청 ID',
  `client_ip` varchar(45) NULL COMMENT '접속 IP',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '처리 일시',
  PRIMARY KEY (`id`),
  KEY `idx_admin_audit_logs_1` (`admin_id`, `created_at`),
  KEY `idx_admin_audit_logs_2` (`target_user_id`, `created_at`),
  KEY `idx_admin_audit_logs_3` (`target_table`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='관리자 변경 이력';

-- ===== 외래 키 =====
ALTER TABLE `users` ADD CONSTRAINT `fk_users_1` FOREIGN KEY (`university_id`) REFERENCES `universities` (`id`);
ALTER TABLE `users` ADD CONSTRAINT `fk_users_2` FOREIGN KEY (`college_id`) REFERENCES `colleges` (`id`);
ALTER TABLE `users` ADD CONSTRAINT `fk_users_3` FOREIGN KEY (`major_id`) REFERENCES `majors` (`id`);
ALTER TABLE `user_tokens` ADD CONSTRAINT `fk_user_tokens_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
ALTER TABLE `user_certificates` ADD CONSTRAINT `fk_user_certificates_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
ALTER TABLE `user_certificates` ADD CONSTRAINT `fk_user_certificates_2` FOREIGN KEY (`jmcd`) REFERENCES `qualifications` (`jmcd`);
ALTER TABLE `account_status_histories` ADD CONSTRAINT `fk_account_status_histories_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
ALTER TABLE `account_status_histories` ADD CONSTRAINT `fk_account_status_histories_2` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`);
ALTER TABLE `colleges` ADD CONSTRAINT `fk_colleges_1` FOREIGN KEY (`university_id`) REFERENCES `universities` (`id`);
ALTER TABLE `majors` ADD CONSTRAINT `fk_majors_1` FOREIGN KEY (`university_id`) REFERENCES `universities` (`id`);
ALTER TABLE `majors` ADD CONSTRAINT `fk_majors_2` FOREIGN KEY (`college_id`) REFERENCES `colleges` (`id`);
ALTER TABLE `majors` ADD CONSTRAINT `fk_majors_3` FOREIGN KEY (`aggregate_college_id`) REFERENCES `colleges` (`id`);
ALTER TABLE `job_categories` ADD CONSTRAINT `fk_job_categories_1` FOREIGN KEY (`interest_field_code`) REFERENCES `interest_fields` (`code`);
ALTER TABLE `major_qualification_links` ADD CONSTRAINT `fk_major_qualification_links_1` FOREIGN KEY (`major_id`) REFERENCES `majors` (`id`) ON DELETE CASCADE;
ALTER TABLE `major_qualification_links` ADD CONSTRAINT `fk_major_qualification_links_2` FOREIGN KEY (`jmcd`) REFERENCES `qualifications` (`jmcd`);
ALTER TABLE `qualifications` ADD CONSTRAINT `fk_qualifications_1` FOREIGN KEY (`obligfldcd`) REFERENCES `interest_fields` (`code`);
ALTER TABLE `qualifications` ADD CONSTRAINT `fk_qualifications_2` FOREIGN KEY (`mdobligfldcd`) REFERENCES `job_categories` (`code`);
ALTER TABLE `qualification_statistics_yearly` ADD CONSTRAINT `fk_qualification_statistics_yearly_1` FOREIGN KEY (`jmcd`) REFERENCES `qualifications` (`jmcd`) ON DELETE CASCADE;
ALTER TABLE `qualification_exam_schedules` ADD CONSTRAINT `fk_qualification_exam_schedules_1` FOREIGN KEY (`jmcd`) REFERENCES `qualifications` (`jmcd`) ON DELETE CASCADE;
ALTER TABLE `favorites` ADD CONSTRAINT `fk_favorites_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
ALTER TABLE `favorites` ADD CONSTRAINT `fk_favorites_2` FOREIGN KEY (`jmcd`) REFERENCES `qualifications` (`jmcd`);
ALTER TABLE `target_exams` ADD CONSTRAINT `fk_target_exams_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
ALTER TABLE `target_exams` ADD CONSTRAINT `fk_target_exams_2` FOREIGN KEY (`jmcd`) REFERENCES `qualifications` (`jmcd`);
ALTER TABLE `target_exams` ADD CONSTRAINT `fk_target_exams_3` FOREIGN KEY (`exam_schedule_id`) REFERENCES `qualification_exam_schedules` (`id`) ON DELETE SET NULL;
ALTER TABLE `planners` ADD CONSTRAINT `fk_planners_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
ALTER TABLE `personal_schedules` ADD CONSTRAINT `fk_personal_schedules_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
ALTER TABLE `planner_bars` ADD CONSTRAINT `fk_planner_bars_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
ALTER TABLE `planner_bars` ADD CONSTRAINT `fk_planner_bars_2` FOREIGN KEY (`target_exam_id`) REFERENCES `target_exams` (`id`) ON DELETE CASCADE;
ALTER TABLE `planner_bars` ADD CONSTRAINT `fk_planner_bars_3` FOREIGN KEY (`personal_schedule_id`) REFERENCES `personal_schedules` (`id`) ON DELETE CASCADE;
ALTER TABLE `planner_milestones` ADD CONSTRAINT `fk_planner_milestones_1` FOREIGN KEY (`bar_id`) REFERENCES `planner_bars` (`id`) ON DELETE CASCADE;
ALTER TABLE `planner_milestones` ADD CONSTRAINT `fk_planner_milestones_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
ALTER TABLE `memos` ADD CONSTRAINT `fk_memos_1` FOREIGN KEY (`target_exam_id`) REFERENCES `target_exams` (`id`) ON DELETE CASCADE;
ALTER TABLE `pass_stories` ADD CONSTRAINT `fk_pass_stories_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
ALTER TABLE `pass_stories` ADD CONSTRAINT `fk_pass_stories_2` FOREIGN KEY (`jmcd`) REFERENCES `qualifications` (`jmcd`);
ALTER TABLE `pass_story_reports` ADD CONSTRAINT `fk_pass_story_reports_1` FOREIGN KEY (`story_id`) REFERENCES `pass_stories` (`id`);
ALTER TABLE `pass_story_reports` ADD CONSTRAINT `fk_pass_story_reports_2` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`);
ALTER TABLE `pass_story_moderation_logs` ADD CONSTRAINT `fk_pass_story_moderation_logs_1` FOREIGN KEY (`story_id`) REFERENCES `pass_stories` (`id`);
ALTER TABLE `pass_story_moderation_logs` ADD CONSTRAINT `fk_pass_story_moderation_logs_2` FOREIGN KEY (`actor_id`) REFERENCES `users` (`id`);
ALTER TABLE `pass_story_moderation_logs` ADD CONSTRAINT `fk_pass_story_moderation_logs_3` FOREIGN KEY (`report_id`) REFERENCES `pass_story_reports` (`id`);
ALTER TABLE `user_ranks` ADD CONSTRAINT `fk_user_ranks_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
ALTER TABLE `notices` ADD CONSTRAINT `fk_notices_1` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`);
ALTER TABLE `inquiries` ADD CONSTRAINT `fk_inquiries_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
ALTER TABLE `inquiry_answers` ADD CONSTRAINT `fk_inquiry_answers_1` FOREIGN KEY (`inquiry_id`) REFERENCES `inquiries` (`id`) ON DELETE CASCADE;
ALTER TABLE `inquiry_answers` ADD CONSTRAINT `fk_inquiry_answers_2` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`);
ALTER TABLE `admin_audit_logs` ADD CONSTRAINT `fk_admin_audit_logs_1` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`);
ALTER TABLE `admin_audit_logs` ADD CONSTRAINT `fk_admin_audit_logs_2` FOREIGN KEY (`target_user_id`) REFERENCES `users` (`id`);
