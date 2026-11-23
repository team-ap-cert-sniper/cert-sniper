import { useEffect, useState } from 'react';
import { getDailyIdiom } from '@/api/plannerApi';

export default function DailyQuote() {
  const [idiom, setIdiom] = useState(null);

  useEffect(() => {
    getDailyIdiom()
      .then(setIdiom)
      .catch(() => setIdiom(null));
  }, []);

  if (!idiom) return null;

  return (
    <div className="daily-quote">
      <p className="daily-quote__label">오늘의 사자성어</p>
      <p className="daily-quote__idiom">
        {idiom.idiom} <span>({idiom.hanja})</span>
      </p>
      <p className="daily-quote__meaning">{idiom.meaning}</p>
    </div>
  );
}
