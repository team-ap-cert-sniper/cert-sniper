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
      <h2 className="mypage-title">
        관심 자격증 {items && <span className="ps-chip ps-chip--pink">총 {items.length}건</span>}
      </h2>
      {error && <p className="field-error">{error}</p>}
      {items?.length === 0 && (
        <div className="mypage-card">
          <EmptyState message="찜한 자격증이 없습니다.">
            <Link to="/recommend">자격증 추천 받으러 가기</Link>
          </EmptyState>
        </div>
      )}
      <div className="mypage-list">
        {items?.map((f) => (
          <article key={f.jmcd} className="fav-card mypage-card">
            <button
              type="button"
              className="fav-card__heart"
              aria-label={`${f.jmfldnm} 찜 해제`}
              title="찜 해제"
              onClick={() => remove(f.jmcd)}
            >
              ♥
            </button>
            <div className="fav-card__body">
              <h3>
                {f.jmfldnm}
                {f.qualgbnm && <span className="ps-chip">{f.qualgbnm}</span>}
              </h3>
              {f.grade_level && <p className="mypage-muted">{f.grade_level}</p>}
              {f.summary && <p>{f.summary}</p>}
            </div>
            <div className="fav-card__actions">
              <Link to={`/qualifications/${f.jmcd}`} className="btn">
                상세보기 ›
              </Link>
              <Link to={`/ai-planner?jmcd=${encodeURIComponent(f.jmcd)}`} className="btn">
                AI 플래너 만들기
              </Link>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
