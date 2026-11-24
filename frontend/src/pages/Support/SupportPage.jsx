import { useSearchParams } from 'react-router-dom';
import NoticeSection from '@/pages/Support/components/NoticeSection';
import FaqSection from '@/pages/Support/components/FaqSection';
import InquiryFormSection from '@/pages/Support/components/InquiryFormSection';
import MyInquirySection from '@/pages/Support/components/MyInquirySection';

// /support?menu=notices|faq|inquiry|my-inquiries (상세는 &id=, 목록 페이지는 &page=)
const MENUS = [
  { key: 'notices', label: '공지사항', Component: NoticeSection },
  { key: 'faq', label: 'FAQ', Component: FaqSection },
  { key: 'inquiry', label: '1:1 문의하기', Component: InquiryFormSection },
  { key: 'my-inquiries', label: '나의 문의 내역', Component: MyInquirySection },
];

export default function SupportPage() {
  const [params, setParams] = useSearchParams();
  const current = MENUS.find((m) => m.key === params.get('menu')) ?? MENUS[0];
  const { Component } = current;

  return (
    <section>
      <h1>고객센터</h1>
      <nav aria-label="고객센터 메뉴">
        {MENUS.map((m) => (
          <button
            key={m.key}
            type="button"
            className="btn"
            aria-current={m.key === current.key ? 'page' : undefined}
            onClick={() => setParams({ menu: m.key })}
          >
            {m.label}
          </button>
        ))}
      </nav>
      <Component />
    </section>
  );
}
