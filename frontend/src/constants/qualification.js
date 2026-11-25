export const QUALIFICATION_TYPES = [
  { value: 'T', label: '국가기술자격' },
  { value: 'S', label: '국가전문자격' },
];

export const GRADE_LEVELS = ['기능사', '산업기사', '기사', '기능장', '기술사', '서비스'];

// 같은 해 안에서 시험 단계 표시 순서 (필기→실기, 1차→2차→3차→면접)
const STAGE_ORDER = { 필기: 0, '1차': 1, '2차': 2, '3차': 3, 실기: 4, 면접: 5, 시험: 6 };
export const stageOrder = (stage) => STAGE_ORDER[stage] ?? 99;
