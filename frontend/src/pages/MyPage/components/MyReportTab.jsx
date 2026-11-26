import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMyReports } from '@/api/reportApi';
import EmptyState from '@/components/common/EmptyState';
import Loading from '@/components/common/Loading';
import Pagination from '@/components/common/Pagination';
import { REPORT_LABELS, REPORT_STATUS_LABELS } from '@/components/passstory/labels';
import { errorMessage } from '@/components/passstory/utils';
import { formatDate } from '@/utils/date';

const PAGE_SIZE = 10;

export default function MyReportTab() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getMyReports({ page, size: PAGE_SIZE })
      .then(setData)
      .catch((e) => setError(errorMessage(e)));
  }, [page]);

  return (
    <section className="mypage-card">
      <h2 className="mypage-title">신고내역 {data && <span className="ps-chip ps-chip--danger">{data.total}</span>}</h2>
      <div className="mypage-notice">
        <strong>접수된 신고 및 제재 내역 안내</strong>
        <p>
          신고된 글은 관리자 검토 전까지 숨김 처리됩니다. 내 글이 신고로 숨김 확정된 횟수가 누적 3회면 3일, 4회면 7일,
          5회부터 30일 동안 이용이 정지됩니다.
        </p>
      </div>
      {error && <p className="field-error">{error}</p>}
      {!data && !error && <Loading />}
      {data?.items.length === 0 && <EmptyState message="신고 당한 내역이 없습니다." />}
      {data?.items.length > 0 && (
        <>
          <ul className="mypage-list report-list">
            {data.items.map((r, i) => (
              <li key={`${r.story_id}-${r.created_at}-${i}`} className={`report-card report-card--${r.status}`}>
                <p className="report-card__head">
                  <span className="ps-chip ps-chip--danger">{REPORT_STATUS_LABELS[r.status]}</span>
                  <span className="mypage-muted">신고 접수일: {formatDate(r.created_at)}</span>
                </p>
                <div className="report-card__panel">
                  <Link to={`/pass-stories/${r.story_id}`} className="ps-chip">
                    {r.jmfldnm} ›
                  </Link>
                  <p className="report-card__reason">! 신고 사유: {REPORT_LABELS[r.report_type]}</p>
                </div>
              </li>
            ))}
          </ul>
          <Pagination page={data.page} total={data.total} size={data.size} onChange={setPage} />
        </>
      )}
    </section>
  );
}
