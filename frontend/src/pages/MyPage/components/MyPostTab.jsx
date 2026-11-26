import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getPassStories } from '@/api/passStoryApi';
import EmptyState from '@/components/common/EmptyState';
import Loading from '@/components/common/Loading';
import Pagination from '@/components/common/Pagination';
import HiddenNotice from '@/components/passstory/HiddenNotice';
import PassStoryCard from '@/components/passstory/PassStoryCard';
import { errorMessage } from '@/components/passstory/utils';

const PAGE_SIZE = 5;

export default function MyPostTab() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getPassStories({ mine: true, page, size: PAGE_SIZE })
      .then(setData)
      .catch((e) => setError(errorMessage(e)));
  }, [page]);

  if (error) return <p className="field-error">{error}</p>;
  if (!data) return <Loading />;

  return (
    <section>
      <h2 className="mypage-title">
        내 작성글 <span className="ps-chip ps-chip--pink">총 {data.total}건</span>
      </h2>
      {data.items.length === 0 && (
        <div className="mypage-card">
          <EmptyState message="작성한 합격수기가 없습니다.">
            <Link to="/pass-stories/new">합격수기 쓰러 가기</Link>
          </EmptyState>
        </div>
      )}
      <div className="mypage-list">
        {data.items.map((story) => (
          <div key={story.id} className="mypage-post">
            <PassStoryCard story={story} />
            {/* 숨김 사유는 상세에서 확인 후 수정 (카드 응답에는 사유가 없음) */}
            <HiddenNotice status={story.status} />
          </div>
        ))}
      </div>
      <Pagination page={data.page} total={data.total} size={data.size} onChange={setPage} />
    </section>
  );
}
