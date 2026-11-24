import { useState } from 'react';
import FAQ_ITEMS from '@/pages/Support/components/faqData';

// CS-03 FAQ: 질문을 누르면 답변이 펼쳐지고, 다시 누르면 접힘
export default function FaqSection() {
  const [openIndex, setOpenIndex] = useState(null);

  return (
    <div>
      <h2>자주 묻는 질문</h2>
      <ul>
        {FAQ_ITEMS.map((item, index) => {
          const open = openIndex === index;
          return (
            <li key={item.question}>
              <button
                type="button"
                className="btn--link"
                aria-expanded={open}
                onClick={() => setOpenIndex(open ? null : index)}
              >
                Q. {item.question}
              </button>
              {open && <p>A. {item.answer}</p>}
            </li>
          );
        })}
      </ul>
    </div>
  );
}
