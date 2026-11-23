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
    <section>
      <h2>신고내역</h2>
      <p>
        제재 기준: 내 글이 신고로 숨김 확정된 횟수가 누적 3회면 3일, 4회면 7일, 5회부터 30일 동안 이용이 정지됩니다.
      </p>
      {error && <p className="field-error">{error}</p>}
      {!data && !error && <Loading />}
      {data?.items.length === 0 && <EmptyState message="신고 당한 내역이 없습니다." />}
      {data?.items.length > 0 && (
        <>
          <table>
            <thead>
              <tr>
                <th scope="col">신고일</th>
                <th scope="col">자격증</th>
                <th scope="col">사유</th>
                <th scope="col">처리 상태</th>
              </tr>
            </thead>
            <tbody>
              {data.items.map((r, i) => (
                <tr key={`${r.story_id}-${r.created_at}-${i}`}>
                  <td>{formatDate(r.created_at)}</td>
                  <td>
                    <Link to={`/pass-stories/${r.story_id}`}>{r.jmfldnm}</Link>
                  </td>
                  <td>{REPORT_LABELS[r.report_type]}</td>
                  <td>{REPORT_STATUS_LABELS[r.status]}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <Pagination page={data.page} total={data.total} size={data.size} onChange={setPage} />
        </>
      )}
    </section>
  );
}
