export function formatNumber(value) {
  if (value === null || value === undefined) return '-';
  return Number(value).toLocaleString('ko-KR');
}

export function formatPercent(value, digits = 1) {
  if (value === null || value === undefined) return '-';
  return `${Number(value).toFixed(digits)}%`;
}
