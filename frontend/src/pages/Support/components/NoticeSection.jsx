import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getNotice, getNotices } from '@/api/supportApi';
import EmptyState from '@/components/common/EmptyState';
import Loading from '@/components/common/Loading';
import Pagination from '@/components/common/Pagination';
import { Breadcrumb, PageHead } from '@/pages/Support/components/SupportParts';
import {
  NOTICE_TYPE_LABELS,
  PAGE_SIZE,
  errorMessage,
  formatDateTime,
  parsePage,
  rowNumber,
} from '@/pages/Support/supportShared';
import { formatDate } from '@/utils/date';
import iconCalendar from '@/pages/Support/assets/calendar.svg';
import iconListWhite from '@/pages/Support/assets/list-white.svg';
import iconMaintenance from '@/pages/Support/assets/notice-maintenance.svg';

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
    <>
      <PageHead crumbs={['고객센터', '공지사항']} title="공지사항" />
      <NoticeList page={page} onPage={goList} onSelect={goDetail} />
    </>
  );
}

function NoticeTypeBadge({ type }) {
  const label = NOTICE_TYPE_LABELS[type] ?? type;
  if (type === 'MAINTENANCE') {
    return (
      <span className="support-pill support-pill--pink">
        <img src={iconMaintenance} alt="" />
        {label}
      </span>
    );
  }
  return <span className="support-pill support-pill--soft">{label}</span>;
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

  let body;
  if (error)
    body = (
      <p className="support-error" role="alert">
        {error}
      </p>
    );
  else if (!data) body = <Loading />;
  else if (data.items.length === 0) body = <EmptyState message="등록된 공지사항이 없습니다." />;
  else {
    body = (
      <>
        <table className="support-table">
          <thead>
            <tr>
              <th className="support-table__center">번호</th>
              <th>제목</th>
              <th className="support-table__center">작성일</th>
            </tr>
          </thead>
          <tbody>
            {data.items.map((notice, i) => (
              <tr key={notice.id}>
                <td className="support-table__num">{rowNumber(data, i)}</td>
                <td>
                  <button type="button" className="support-table__link" onClick={() => onSelect(notice.id)}>
                    {notice.notice_type === 'MAINTENANCE' && (
                      <span className="support-tag support-tag--pink">{NOTICE_TYPE_LABELS.MAINTENANCE}</span>
                    )}
                    {notice.title}
                  </button>
                </td>
                <td className="support-table__date">{formatDate(notice.created_at)}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {data.total > data.size && (
          <div className="support-table-foot">
            <Pagination page={data.page} total={data.total} size={data.size} onChange={onPage} />
          </div>
        )}
      </>
    );
  }

  return <div className="support-card support-card--clip">{body}</div>;
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
    <>
      <Breadcrumb items={['고객센터', '공지사항', '세부내용']} home />
      <article className="support-card support-card--clip">
        {error && (
          <p className="support-error" role="alert">
            {error}
          </p>
        )}
        {!error && !notice && <Loading />}
        {notice && (
          <>
            <header className="support-article__head">
              <NoticeTypeBadge type={notice.notice_type} />
              <h2 className="support-article__title">{notice.title}</h2>
              <p className="support-meta">
                <img src={iconCalendar} alt="" />
                작성일: {formatDateTime(notice.created_at)}
              </p>
            </header>
            {/* 본문은 서버에서 허용 태그만 남겨 저장함 (HtmlSanitizer) */}
            <div className="support-article__body" dangerouslySetInnerHTML={{ __html: notice.content }} />
          </>
        )}
        <div className="support-article__actions">
          <button type="button" className="support-btn support-btn--plum support-btn--sm" onClick={onBack}>
            <img src={iconListWhite} alt="" />
            목록으로
          </button>
        </div>
      </article>
    </>
  );
}
