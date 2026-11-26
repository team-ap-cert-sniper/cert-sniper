import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getMyInquiries, getMyInquiry } from '@/api/supportApi';
import EmptyState from '@/components/common/EmptyState';
import Loading from '@/components/common/Loading';
import Pagination from '@/components/common/Pagination';
import { INQUIRY_STATUS } from '@/constants/inquiry';
import { PAGE_SIZE, errorMessage, inquiryTypeLabel, parsePage } from '@/pages/Support/supportShared';
import { formatDate } from '@/utils/date';

// CS-05 나의 문의 내역: 목록(5개씩)과 상세. 상세에서 목록으로 가면 보던 페이지 유지
export default function MyInquirySection() {
  const [params, setParams] = useSearchParams();
  const id = params.get('id');
  const page = parsePage(params.get('page'));

  const goList = (p) => setParams({ menu: 'my-inquiries', page: String(p) });
  const goDetail = (inquiryId) => setParams({ menu: 'my-inquiries', page: String(page), id: String(inquiryId) });

  return id ? (
    <InquiryDetail inquiryId={id} onBack={() => goList(page)} />
  ) : (
    <InquiryList page={page} onPage={goList} onSelect={goDetail} />
  );
}

function InquiryList({ page, onPage, onSelect }) {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let ignore = false;
    getMyInquiries({ page, size: PAGE_SIZE })
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
  if (data.items.length === 0) return <EmptyState message="문의 내역이 없습니다." />;

  return (
    <div>
      <h2>나의 문의 내역</h2>
      <table>
        <thead>
          <tr>
            <th>유형</th>
            <th>제목</th>
            <th>상태</th>
            <th>작성일</th>
          </tr>
        </thead>
        <tbody>
          {data.items.map((inquiry) => (
            <tr key={inquiry.id}>
              <td>{inquiryTypeLabel(inquiry.inquiry_type)}</td>
              <td>
                <button type="button" className="btn--link" onClick={() => onSelect(inquiry.id)}>
                  {inquiry.title}
                </button>
              </td>
              <td>{INQUIRY_STATUS[inquiry.status] ?? inquiry.status}</td>
              <td>{formatDate(inquiry.created_at)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <Pagination page={data.page} total={data.total} size={data.size} onChange={onPage} />
    </div>
  );
}

// 입력한 줄바꿈을 그대로 보여 줌 (CSS 없이)
function MultilineText({ text }) {
  return (
    <p>
      {text.split('\n').map((line, i) => (
        <span key={i}>
          {i > 0 && <br />}
          {line}
        </span>
      ))}
    </p>
  );
}

function InquiryDetail({ inquiryId, onBack }) {
  const [inquiry, setInquiry] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let ignore = false;
    getMyInquiry(inquiryId)
      .then((result) => {
        if (ignore) return;
        setInquiry(result);
        setError('');
      })
      .catch((e) => {
        if (!ignore) setError(errorMessage(e, '문의를 찾을 수 없습니다.'));
      });
    return () => {
      ignore = true;
    };
  }, [inquiryId]);

  return (
    <div>
      {error && <p role="alert">{error}</p>}
      {!error && !inquiry && <Loading />}
      {inquiry && (
        <article>
          <p>
            {inquiryTypeLabel(inquiry.inquiry_type)} · {INQUIRY_STATUS[inquiry.status] ?? inquiry.status}
          </p>
          <h2>{inquiry.title}</h2>
          <p>작성일 {formatDate(inquiry.created_at)}</p>
          <MultilineText text={inquiry.content} />

          <h3>답변</h3>
          {inquiry.answer ? <MultilineText text={inquiry.answer} /> : <p>아직 답변이 등록되지 않았습니다.</p>}
        </article>
      )}
      <button type="button" className="btn" onClick={onBack}>
        목록으로
      </button>
    </div>
  );
}
