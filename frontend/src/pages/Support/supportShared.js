import { INQUIRY_TYPES } from '@/constants/inquiry';
import { formatDate } from '@/utils/date';

// 고객센터 목록은 5개씩 (개발 시작 공지)
export const PAGE_SIZE = 5;

// notices.notice_type 표시명 (Q12 결정 전까지 이 폴더에 둠)
export const NOTICE_TYPE_LABELS = {
  GENERAL: '일반',
  MAINTENANCE: '시스템 점검',
};

// 주소의 page 값이 0 이상 정수가 아니면 첫 페이지로
export const parsePage = (value) => {
  const page = Number(value);
  return Number.isInteger(page) && page >= 0 ? page : 0;
};

export const inquiryTypeLabel = (value) => INQUIRY_TYPES.find((t) => t.value === value)?.label ?? value;

// 목록 표에 쓰는 짧은 문의 유형 이름 (시안의 유형 태그)
const INQUIRY_TYPE_SHORT = {
  ACCOUNT: '계정/로그인',
  PLANNER: '플래너',
  SUGGESTION: '기능 제안',
  CERT_DATA: '자격증 정보',
  ETC: '기타',
};

export const inquiryTypeShortLabel = (value) => INQUIRY_TYPE_SHORT[value] ?? inquiryTypeLabel(value);

// 2026.11.25 14:22
export const formatDateTime = (value) => {
  if (!value) return '';
  const d = new Date(value);
  const pad = (n) => String(n).padStart(2, '0');
  return `${formatDate(value)} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

// 목록 번호: 최신 글이 가장 큰 번호
export const rowNumber = (data, index) => data.total - data.page * data.size - index;

export const errorMessage = (e, fallback = '요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.') =>
  e.response?.data?.message ?? fallback;
