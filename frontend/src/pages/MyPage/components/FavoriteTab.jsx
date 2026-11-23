import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getFavorites, removeFavorite } from '@/api/favoriteApi';
import EmptyState from '@/components/common/EmptyState';
import Loading from '@/components/common/Loading';
import { errorMessage } from '@/components/passstory/utils';

export default function FavoriteTab() {
  const [items, setItems] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getFavorites()
      .then(setItems)
      .catch((e) => setError(errorMessage(e)));
  }, []);

  const remove = (jmcd) =>
    removeFavorite(jmcd)
      .then(() => setItems((list) => list.filter((f) => f.jmcd !== jmcd)))
      .catch((e) => setError(errorMessage(e)));

  if (!items && !error) return <Loading />;

  return (
    <section>
      <h2>관심 자격증 {items && `(${items.length}개)`}</h2>
      {error && <p className="field-error">{error}</p>}
      {items?.length === 0 && (
        <EmptyState message="찜한 자격증이 없습니다.">
          <Link to="/recommend">자격증 추천 받으러 가기</Link>
        </EmptyState>
      )}
      {items?.map((f) => (
        <article key={f.jmcd}>
          <h3>{f.jmfldnm}</h3>
          <p>
            {f.qualgbnm}
            {f.grade_level && ` · ${f.grade_level}`}
          </p>
          <p>{f.summary}</p>
          <Link to={`/qualifications/${f.jmcd}`} className="btn">
            상세보기
          </Link>
          <Link to={`/ai-planner?jmcd=${encodeURIComponent(f.jmcd)}`} className="btn">
            AI 플래너 만들기
          </Link>
          <button type="button" className="btn" onClick={() => remove(f.jmcd)}>
            찜 해제
          </button>
        </article>
      ))}
    </section>
  );
}
