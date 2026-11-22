export const INQUIRY_TYPES = [
  { value: 'ACCOUNT', label: '계정/로그인/회원정보 관련' },
  { value: 'PLANNER', label: '캘린더 및 학습 플래너 오류/기능 문의' },
  { value: 'SUGGESTION', label: '서비스 기능 제안 및 개선 의견' },
  { value: 'CERT_DATA', label: '자격증 정보/일정/통계 오류 제보' },
  { value: 'ETC', label: '기타 문의' },
];

export const INQUIRY_STATUS = {
  WAITING: '답변 대기',
  ANSWERED: '답변 완료',
};
