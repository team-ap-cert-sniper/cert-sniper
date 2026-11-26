// 서버 코드값 → 화면 문구. shortcut: 문구는 임시, 기획 확정 시 교체 (공통 constants/ 이동은 kiwan.md 안건)
export const RANK_LABELS = {
  TRAINEE: '훈련병',
  APPRENTICE: '견습 스나이퍼',
  MARKSMAN: '명사수',
  LEGEND: '전설의 스나이퍼',
};

export const STATUS_LABELS = {
  PUBLISHED: '공개',
  HIDDEN: '숨김',
  REVIEW_PENDING: '재검토 대기',
};

export const REPORT_LABELS = {
  IRRELEVANT: '관련 없는 내용',
  ABUSE: '욕설·비방',
  PRIVACY: '개인정보 노출',
  AD: '광고·홍보',
  ETC: '기타',
};

export const REPORT_DESCRIPTIONS = {
  IRRELEVANT: '주제와 무관하거나 도배성 내용',
  ABUSE: '타인을 비하하거나 불쾌감 조성',
  PRIVACY: '연락처, 계좌번호, 사생활 등 노출',
  AD: '상업적 영리 목적 또는 스팸 링크',
  ETC: '위 분류에 해당하지 않는 기타 사항',
};

export const REPORT_STATUS_LABELS = {
  PENDING: '관리자 검토 중 (숨김)',
  CONFIRMED: '숨김 확정',
  DISMISSED: '기각 (숨김 해제)',
};

export const EXAMINEE_BASE_LABELS = {
  NO_BASE: '노베이스',
  BASIC: '기초 지식 있음',
  MAJOR: '전공자',
  PRACTITIONER: '실무자',
};

export const PREP_PERIOD_LABELS = {
  UNDER_1M: '1개월 미만',
  M1_TO_3M: '1~3개월',
  M3_TO_6M: '3~6개월',
  OVER_6M: '6개월 이상',
};

export const ATTEMPT_LABELS = {
  1: '1회',
  2: '2회',
  3: '3회',
  '4_PLUS': '4회 이상',
};
