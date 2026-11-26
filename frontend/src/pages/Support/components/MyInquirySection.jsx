import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getMyInquiries, getMyInquiry } from '@/api/supportApi';
import EmptyState from '@/components/common/EmptyState';
import Loading from '@/components/common/Loading';
import Pagination from '@/components/common/Pagination';
import { INQUIRY_STATUS } from '@/constants/inquiry';
import FAQ_ITEMS from '@/pages/Support/components/faqData';
import { Breadcrumb, PageHead } from '@/pages/Support/components/SupportParts';
import {
  PAGE_SIZE,
  errorMessage,
  formatDateTime,
  inquiryTypeLabel,
  inquiryTypeShortLabel,
  parsePage,
  rowNumber,
} from '@/pages/Support/supportShared';
import { formatDate } from '@/utils/date';
import iconArrowSmall from '@/pages/Support/assets/arrow-small.svg';
import iconBack from '@/pages/Support/assets/back.svg';
import iconCheckWhite from '@/pages/Support/assets/check-white.svg';
import iconCompose from '@/pages/Support/assets/compose.svg';
import iconComposeWhite from '@/pages/Support/assets/compose-white.svg';
import iconFaqBook from '@/pages/Support/assets/faq-book.svg';
import iconHelpChat from '@/pages/Support/assets/help-chat.svg';
import iconAnswer from '@/pages/Support/assets/inquiry-answer.svg';
import iconPen from '@/pages/Support/assets/inquiry-pen.svg';
import iconQuestion from '@/pages/Support/assets/inquiry-question.svg';
import iconListDark from '@/pages/Support/assets/list-dark.svg';

// 아래 "자주 묻는 질문 둘러보기"에 보여 줄 FAQ 번호
const FAQ_SHORTCUTS = [2, 6];

// CS-05 나의 문의 내역: 목록(5개씩)과 상세. 상세에서 목록으로 가면 보던 페이지 유지
export default function MyInquirySection() {
  const [params, setParams] = useSearchParams();
  const id = params.get('id');
  const page = parsePage(params.get('page'));

  const goList = (p) => setParams({ menu: 'my-inquiries', page: String(p) });
  const goDetail = (inquiryId) => setParams({ menu: 'my-inquiries', page: String(page), id: String(inquiryId) });
  const goWrite = () => setParams({ menu: 'inquiry' });
  const goFaq = (index) => setParams({ menu: 'faq', faq: String(index) });

  return id ? (
    <InquiryDetail inquiryId={id} onBack={() => goList(page)} onWrite={goWrite} />
  ) : (
    <>
      <PageHead
        crumbs={['고객센터', '나의 문의 내역']}
        title="나의 문의 내역"
        action={
          <button type="button" className="support-btn support-btn--primary support-btn--sm" onClick={goWrite}>
            <img src={iconCompose} alt="" />새 문의 작성하기
          </button>
        }
      />
      <InquiryList page={page} onPage={goList} onSelect={goDetail} />
      <BottomCards onWrite={goWrite} onFaq={goFaq} />
    </>
  );
}

function StatusPill({ status }) {
  const label = INQUIRY_STATUS[status] ?? status;
  if (status === 'WAITING') {
    return (
      <span className="support-pill support-pill--waiting">
        <span className="support-pill__dot" />
        {label}
      </span>
    );
  }
  return <span className="support-pill support-pill--answered">{label}</span>;
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

  let body;
  if (error)
    body = (
      <p className="support-error" role="alert">
        {error}
      </p>
    );
  else if (!data) body = <Loading />;
  else if (data.items.length === 0) body = <EmptyState message="문의 내역이 없습니다." />;
  else {
    const first = data.page * data.size + 1;
    const last = data.page * data.size + data.items.length;
    body = (
      <>
        <table className="support-table support-table--inquiry">
          <thead>
            <tr>
              <th className="support-table__center">번호</th>
              <th>문의 유형</th>
              <th>문의 제목</th>
              <th className="support-table__center">작성일</th>
              <th className="support-table__center">답변 상태</th>
            </tr>
          </thead>
          <tbody>
            {data.items.map((inquiry, i) => (
              <tr key={inquiry.id}>
                <td className="support-table__num">{rowNumber(data, i)}</td>
                <td>
                  <span className="support-tag">{inquiryTypeShortLabel(inquiry.inquiry_type)}</span>
                </td>
                <td>
                  <button type="button" className="support-table__link" onClick={() => onSelect(inquiry.id)}>
                    {inquiry.title}
                  </button>
                </td>
                <td className="support-table__date">{formatDate(inquiry.created_at)}</td>
                <td className="support-table__center">
                  <StatusPill status={inquiry.status} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="support-table-foot support-table-foot--split">
          <span>
            {first}-{last} / 총 {data.total}건 표시
          </span>
          <Pagination page={data.page} total={data.total} size={data.size} onChange={onPage} />
        </div>
      </>
    );
  }

  return (
    <div className="support-card support-card--clip">
      <div className="support-list-toolbar">
        <h3 className="support-list-toolbar__title">문의 목록</h3>
        {data && (
          <span className="support-list-toolbar__count">
            총 <strong>{data.total}</strong>건의 문의가 있습니다.
          </span>
        )}
      </div>
      {body}
    </div>
  );
}

function BottomCards({ onWrite, onFaq }) {
  return (
    <div className="support-bottom">
      <div className="support-card support-help">
        <div className="support-help__text">
          <span className="support-icon-box support-icon-box--pink">
            <img src={iconHelpChat} alt="" />
          </span>
          <div>
            <h3 className="support-help__title">궁금한 점이 아직 해결되지 않으셨나요?</h3>
            <p className="support-help__desc">
              시험 일정 변동, 응시 자격, 맞춤 추천 관련 상세 질문을 남겨 주시면 담당자가 확인 후 답변해 드립니다.
            </p>
          </div>
        </div>
        <button type="button" className="support-btn support-btn--primary support-btn--lg" onClick={onWrite}>
          <img src={iconPen} alt="" />
          1:1 문의하기
        </button>
      </div>

      <div className="support-shortcut">
        <div>
          <h3 className="support-shortcut__title">
            <img src={iconFaqBook} alt="" />
            자주 묻는 질문 둘러보기
          </h3>
          <p className="support-shortcut__desc">많은 회원분들이 궁금해하시는 내용을 빠르게 확인하세요.</p>
        </div>
        <div className="support-shortcut__list">
          {FAQ_SHORTCUTS.filter((i) => FAQ_ITEMS[i]).map((i) => (
            <button key={i} type="button" className="support-shortcut__link" onClick={() => onFaq(i)}>
              {FAQ_ITEMS[i].question}
              <img src={iconArrowSmall} alt="" />
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

function InquiryDetail({ inquiryId, onBack, onWrite }) {
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

  const answered = inquiry?.status === 'ANSWERED';

  return (
    <>
      <div className="support-detail-top">
        <Breadcrumb items={['고객센터', '나의 문의 내역', '상세 정보']} home slash />
        <button type="button" className="support-btn support-btn--secondary support-btn--chip" onClick={onBack}>
          <img src={iconBack} alt="" />
          목록으로
        </button>
      </div>

      {error && (
        <div className="support-card">
          <p className="support-error" role="alert">
            {error}
          </p>
        </div>
      )}
      {!error && !inquiry && (
        <div className="support-card">
          <Loading />
        </div>
      )}
      {inquiry && (
        <>
          <article className="support-card support-hero">
            <div className="support-hero__row">
              <span className="support-pill support-pill--type">
                <span className="support-pill__dot" />
                {inquiryTypeLabel(inquiry.inquiry_type)}
              </span>
              <span
                className={
                  answered ? 'support-status support-status--answered' : 'support-status support-status--waiting'
                }
              >
                {answered ? <img src={iconCheckWhite} alt="" /> : <span className="support-pill__dot" />}
                {INQUIRY_STATUS[inquiry.status] ?? inquiry.status}
              </span>
            </div>
            <h2 className="support-article__title">{inquiry.title}</h2>
            <dl className="support-meta-strip">
              <div>
                <dt className="support-meta-strip__label">등록 일시</dt>
                <dd className="support-meta-strip__value">{formatDateTime(inquiry.created_at)}</dd>
              </div>
            </dl>
          </article>

          <section className="support-card support-block">
            <h3 className="support-block__head">
              <img src={iconQuestion} alt="" />
              문의 내용
            </h3>
            <p className="support-block__body">{inquiry.content}</p>
          </section>

          <section className="support-card support-block support-block--answer">
            <h3 className="support-block__head support-block__head--answer">
              <span className="support-icon-box support-icon-box--answer">
                <img src={iconAnswer} alt="" />
              </span>
              관리자 답변
            </h3>
            {inquiry.answer ? (
              <p className="support-block__body support-block__body--answer">{inquiry.answer}</p>
            ) : (
              <p className="support-block__body support-block__body--answer support-block__body--empty">
                아직 답변이 등록되지 않았습니다. 답변이 등록되면 이곳에서 확인할 수 있습니다.
              </p>
            )}
          </section>
        </>
      )}

      <div className="support-detail-actions">
        <button type="button" className="support-btn support-btn--secondary support-btn--lg" onClick={onBack}>
          <img src={iconListDark} alt="" />
          목록으로 돌아가기
        </button>
        <button type="button" className="support-btn support-btn--plum" onClick={onWrite}>
          <img src={iconComposeWhite} alt="" />
          추가 문의 작성하기
        </button>
      </div>
    </>
  );
}
