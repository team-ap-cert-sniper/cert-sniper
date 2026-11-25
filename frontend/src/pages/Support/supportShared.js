import { INQUIRY_TYPES } from '@/constants/inquiry';

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

export const errorMessage = (e, fallback = '요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.') =>
  e.response?.data?.message ?? fallback;
