const pad = (n) => String(n).padStart(2, '0');

export function toDateString(date) {
  const d = new Date(date);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

export function formatDate(value, separator = '.') {
  if (!value) return '';
  return toDateString(value).replaceAll('-', separator);
}

export function ddayLabel(examDate, today = new Date()) {
  if (!examDate) return '';
  const start = new Date(toDateString(today));
  const target = new Date(toDateString(examDate));
  const days = Math.round((target - start) / 86400000);
  if (days > 0) return `D-${days}`;
  if (days === 0) return 'D-Day';
  return '종료';
}
