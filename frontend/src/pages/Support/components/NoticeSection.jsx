import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getNotice, getNotices } from '@/api/supportApi';
import EmptyState from '@/components/common/EmptyState';
import Loading from '@/components/common/Loading';
import Pagination from '@/components/common/Pagination';
import { NOTICE_TYPE_LABELS, PAGE_SIZE, errorMessage, parsePage } from '@/pages/Support/supportShared';
import { formatDate } from '@/utils/date';

// CS-02 공지사항: 목록(5개씩, 작성일만 표시)과 상세. 상세에서 목록으로 가면 보던 페이지 유지
export default function NoticeSection() {
  const [params, setParams] = useSearchParams();
  const id = params.get('id');
  const page = parsePage(params.get('page'));

  const goList = (p) => setParams({ menu: 'notices', page: String(p) });
  const goDetail = (noticeId) => setParams({ menu: 'notices', page: String(page), id: String(noticeId) });

  return id ? (
    <NoticeDetail noticeId={id} onBack={() => goList(page)} />
  ) : (
    <NoticeList page={page} onPage={goList} onSelect={goDetail} />
  );
}

function NoticeList({ page, onPage, onSelect }) {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let ignore = false;
    getNotices({ page, size: PAGE_SIZE })
      .then((result) => {
        if (ignore) return;
        setData(result);
        setError('');
      })
      .catch((e) => {
        if (!ignore) setError(errorMessage(e));
      });
    return () => {
      ignore = true;
    };
  }, [page]);

  if (error) return <p role="alert">{error}</p>;
  if (!data) return <Loading />;
  if (data.items.length === 0) return <EmptyState message="등록된 공지사항이 없습니다." />;

  return (
    <div>
      <h2>공지사항</h2>
      <table>
        <thead>
          <tr>
            <th>유형</th>
            <th>제목</th>
            <th>작성일</th>
          </tr>
        </thead>
        <tbody>
          {data.items.map((notice) => (
            <tr key={notice.id}>
              <td>{NOTICE_TYPE_LABELS[notice.notice_type] ?? notice.notice_type}</td>
              <td>
                <button type="button" className="btn--link" onClick={() => onSelect(notice.id)}>
                  {notice.title}
                </button>
              </td>
              <td>{formatDate(notice.created_at)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <Pagination page={data.page} total={data.total} size={data.size} onChange={onPage} />
    </div>
  );
}

function NoticeDetail({ noticeId, onBack }) {
  const [notice, setNotice] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let ignore = false;
    getNotice(noticeId)
      .then((result) => {
        if (ignore) return;
        setNotice(result);
        setError('');
      })
      .catch((e) => {
        if (!ignore) setError(errorMessage(e, '공지사항을 찾을 수 없습니다.'));
      });
    return () => {
      ignore = true;
    };
  }, [noticeId]);

  return (
    <div>
      {error && <p role="alert">{error}</p>}
      {!error && !notice && <Loading />}
      {notice && (
        <article>
          <p>{NOTICE_TYPE_LABELS[notice.notice_type] ?? notice.notice_type}</p>
          <h2>{notice.title}</h2>
          <p>작성일 {formatDate(notice.created_at)}</p>
          {/* 본문은 서버에서 허용 태그만 남겨 저장함 (HtmlSanitizer) */}
          <div dangerouslySetInnerHTML={{ __html: notice.content }} />
        </article>
      )}
      <button type="button" className="btn" onClick={onBack}>
        목록으로
      </button>
    </div>
  );
}
