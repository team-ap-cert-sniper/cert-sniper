import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import FAQ_ITEMS from '@/pages/Support/components/faqData';
import { PageHead } from '@/pages/Support/components/SupportParts';
import iconArrow from '@/pages/Support/assets/arrow-right.svg';
import iconChevron from '@/pages/Support/assets/faq-chevron.svg';
import iconHelp from '@/pages/Support/assets/faq-help.svg';

// CS-03 FAQ: 질문을 누르면 답변이 펼쳐지고, 다시 누르면 접힘. ?faq=번호로 들어오면 그 질문을 펼침
export default function FaqSection() {
  const [params, setParams] = useSearchParams();
  // 시안처럼 처음에는 첫 질문을 펼쳐 둠
  const faqParam = params.get('faq');
  const initial = faqParam === null ? 0 : Number(faqParam);
  const [openIndex, setOpenIndex] = useState(Number.isInteger(initial) && FAQ_ITEMS[initial] ? initial : null);

  return (
    <>
      <PageHead
        crumbs={['고객센터', '자주 묻는 질문 (FAQ)']}
        title="자주 묻는 질문"
        pill="FAQ Center"
        desc="저격증 서비스 이용에 대해 수험생 및 취업 준비생분들이 가장 빈번하게 문의하신 내용을 모았습니다."
        large
      />
      <ul className="support-faq">
        {FAQ_ITEMS.map((item, index) => {
          const open = openIndex === index;
          return (
            <li key={item.question} className="support-card support-card--clip">
              <button
                type="button"
                className="support-faq__q"
                aria-expanded={open}
                onClick={() => setOpenIndex(open ? null : index)}
              >
                <span className="support-faq__q-text">
                  <span className="support-faq__mark" aria-hidden="true">
                    Q
                  </span>
                  {item.question}
                </span>
                <span className="support-faq__q-side">
                  {item.category && <span className="support-tag">{item.category}</span>}
                  <img className="support-faq__chevron" src={iconChevron} alt="" />
                </span>
              </button>
              {open && (
                <div className="support-faq__a">
                  <span className="support-faq__mark support-faq__mark--answer" aria-hidden="true">
                    A
                  </span>
                  <p>{item.answer}</p>
                </div>
              )}
            </li>
          );
        })}
      </ul>

      <div className="support-cta">
        <div className="support-cta__text">
          <span className="support-icon-box">
            <img src={iconHelp} alt="" />
          </span>
          <div>
            <h3 className="support-cta__title">원하시는 답변을 찾지 못하셨나요?</h3>
            <p className="support-cta__desc">
              자격증 시험 문의, 시스템 오류 제보 등 상세한 질의는 1:1 문의로 남겨 주시면 확인 후 안내해 드립니다.
            </p>
          </div>
        </div>
        <button
          type="button"
          className="support-btn support-btn--primary"
          onClick={() => setParams({ menu: 'inquiry' })}
        >
          1:1 문의하기
          <img src={iconArrow} alt="" />
        </button>
      </div>
    </>
  );
}
