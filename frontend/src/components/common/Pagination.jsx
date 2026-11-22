const BLOCK = 5;

export default function Pagination({ page, total, size, onChange }) {
  const totalPages = Math.ceil(total / size);
  if (totalPages <= 1) return null;

  const start = Math.floor(page / BLOCK) * BLOCK;
  const end = Math.min(start + BLOCK, totalPages);
  const pages = Array.from({ length: end - start }, (_, i) => start + i);

  return (
    <nav className="pagination">
      <button type="button" className="btn--link" disabled={start === 0} onClick={() => onChange(start - 1)}>
        이전
      </button>
      {pages.map((p) => (
        <button
          key={p}
          type="button"
          className={p === page ? 'btn--link pagination__current' : 'btn--link'}
          onClick={() => onChange(p)}
        >
          {p + 1}
        </button>
      ))}
      <button type="button" className="btn--link" disabled={end >= totalPages} onClick={() => onChange(end)}>
        다음
      </button>
    </nav>
  );
}
