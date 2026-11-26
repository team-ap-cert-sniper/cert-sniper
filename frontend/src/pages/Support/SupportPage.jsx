import { useSearchParams } from 'react-router-dom';
import NoticeSection from '@/pages/Support/components/NoticeSection';
import FaqSection from '@/pages/Support/components/FaqSection';
import InquiryFormSection from '@/pages/Support/components/InquiryFormSection';
import MyInquirySection from '@/pages/Support/components/MyInquirySection';
import iconNotice from '@/pages/Support/assets/menu-notice.svg';
import iconFaq from '@/pages/Support/assets/menu-faq.svg';
import iconInquiry from '@/pages/Support/assets/menu-inquiry.svg';
import iconMyInquiries from '@/pages/Support/assets/menu-my-inquiries.svg';
import '@/styles/support.css';

// /support?menu=notices|faq|inquiry|my-inquiries (상세는 &id=, 목록 페이지는 &page=)
const MENUS = [
  { key: 'notices', label: '공지사항', icon: iconNotice, Component: NoticeSection },
  { key: 'faq', label: '자주 묻는 질문 (FAQ)', icon: iconFaq, Component: FaqSection },
  { key: 'inquiry', label: '1:1 문의하기', icon: iconInquiry, Component: InquiryFormSection },
  { key: 'my-inquiries', label: '나의 문의 내역', icon: iconMyInquiries, Component: MyInquirySection },
];

export default function SupportPage() {
  const [params, setParams] = useSearchParams();
  const current = MENUS.find((m) => m.key === params.get('menu')) ?? MENUS[0];
  const { Component } = current;

  return (
    <section className="support">
      <aside className="support__aside">
        <h1 className="support__aside-title">고객센터 지원실</h1>
        <nav className="support__menu" aria-label="고객센터 메뉴">
          {MENUS.map((m) => (
            <button
              key={m.key}
              type="button"
              className="support__menu-item"
              aria-current={m.key === current.key ? 'page' : undefined}
              onClick={() => setParams({ menu: m.key })}
            >
              <img src={m.icon} alt="" />
              {m.label}
            </button>
          ))}
        </nav>
      </aside>
      <div className="support__main">
        <Component />
      </div>
    </section>
  );
}
