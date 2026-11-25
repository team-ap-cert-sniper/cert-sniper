import { useState } from 'react';
import useOptions from '@/hooks/useOptions';

const SEARCH_LIMIT = 50;

// 종목 검색 팝업: 종목명 부분 일치로 찾고, 고르면 그 자격증 상세로 이동
export default function QualificationSearchModal({ initialQuery = '', onSelect, onClose }) {
  const [input, setInput] = useState(initialQuery);
  const [query, setQuery] = useState(initialQuery.trim());
  const results = useOptions('qualifications', query ? { query, limit: SEARCH_LIMIT } : null);

  const submit = (e) => {
    e.preventDefault();
    setQuery(input.trim());
  };

  const renderResults = () => {
    if (!query) return <p className="qualification-search-modal__placeholder">🔍</p>;
    if (results === null) return <p className="qualification-search-modal__placeholder">찾는 중입니다.</p>;
    if (results.length === 0) return <p className="qualification-search-modal__placeholder">검색 결과가 없습니다.</p>;
    return (
      <ul className="qualification-search-modal__list">
        {results.map((item) => (
          <li key={item.jmcd}>
            <button type="button" onClick={() => onSelect(item.jmcd)}>
              <strong>{item.jmfldnm}</strong>
              <span>{[item.qualgbnm, item.grade_level].filter(Boolean).join(' · ')}</span>
            </button>
          </li>
        ))}
      </ul>
    );
  };

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="qualification-search-title">
      <div className="qualification-search-modal">
        <header className="qualification-search-modal__head">
          <h2 id="qualification-search-title">종목검색</h2>
          <button type="button" aria-label="닫기" onClick={onClose}>
            ✕
          </button>
        </header>
        <form className="qualification-search-modal__form" onSubmit={submit}>
          <label htmlFor="qualification-search-input">응시종목검색</label>
          <input
            id="qualification-search-input"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="종목명을 입력하세요."
            autoFocus
          />
          <button type="submit" className="qualification-button">
            검색
          </button>
        </form>
        <p className="qualification-search-modal__hint">
          정확한 명칭을 모르실 경우 아시는 단어로 검색하십시오. ex) 정보처리기사 → &quot;정보&quot;, 산업안전기사 →
          &quot;안전&quot;
        </p>
        <div className="qualification-search-modal__results">{renderResults()}</div>
      </div>
    </div>
  );
}
