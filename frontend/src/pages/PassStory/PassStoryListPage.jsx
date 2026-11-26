import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { getOptions } from '@/api/optionApi';
import { getPassStories } from '@/api/passStoryApi';
import EmptyState from '@/components/common/EmptyState';
import Loading from '@/components/common/Loading';
import Pagination from '@/components/common/Pagination';
import PassStoryCard from '@/components/passstory/PassStoryCard';
import { errorMessage } from '@/components/passstory/utils';
import RankDashboard from '@/pages/PassStory/components/RankDashboard';
import RankUpModal from '@/pages/PassStory/components/RankUpModal';

const PAGE_SIZE = 5;
const EMPTY = { items: [], total: 0, page: 0, size: PAGE_SIZE };

// 자격증 이름 검색: 이름 → 종목코드(옵션 API) → jmcds로 합격수기 조회
// shortcut: 이름이 일치하는 종목은 앞 100개까지만 반영, 그 이상이 흔해지면 서버 검색(keyword)으로 전환
async function fetchStories(mine, q, page) {
  if (mine || !q) {
    return getPassStories({ mine, page, size: PAGE_SIZE });
  }
  const options = await getOptions('qualifications', { query: q, limit: 100 });
  const jmcds = options.items.map((item) => item.jmcd);
  return jmcds.length ? getPassStories({ jmcds: jmcds.join(','), page, size: PAGE_SIZE }) : EMPTY;
}

export default function PassStoryListPage() {
  const [params, setParams] = useSearchParams();
  const mine = params.get('mine') === 'true';
  const q = params.get('q') ?? '';
  const page = Number(params.get('page') ?? 0);

  const [input, setInput] = useState(q);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  const location = useLocation();
  const navigate = useNavigate();
  const rankUp = location.state?.rankUp;

  useEffect(() => {
    let ignore = false;
    fetchStories(mine, q, page)
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
  }, [mine, q, page]);

  const search = (e) => {
    e.preventDefault();
    const keyword = input.trim();
    setParams(keyword ? { q: keyword } : {});
  };

  const changePage = (p) => {
    const next = new URLSearchParams(params);
    next.set('page', String(p));
    setParams(next);
  };

  const emptyMessage = mine
    ? '작성한 합격수기가 없습니다.'
    : q
      ? '검색 결과가 없습니다.'
      : '아직 등록된 합격수기가 없습니다.';

  return (
    <section className="ps ps-list">
      <header className="ps-head">
        <div>
          <h1>시험 합격 후기</h1>
          <p>실제 합격생들의 생생한 공부법과 검증된 꿀팁을 확인해 보세요</p>
        </div>
        <Link to="/pass-stories/new" className="btn btn--primary">
          ✎ 후기 작성하기
        </Link>
      </header>

      <RankDashboard />

      <div className="ps-toolbar ps-box">
        {!mine && (
          <form onSubmit={search} role="search">
            <label htmlFor="pass-story-search" className="sr-only">
              자격증 이름
            </label>
            <input
              id="pass-story-search"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="자격증 이름으로 검색 (예: SQLD, 정보처리기사)"
            />
            <button type="submit" className="btn">
              검색
            </button>
          </form>
        )}
        <div className="ps-toolbar__tabs">
          <button type="button" className="btn" aria-pressed={!mine} onClick={() => setParams({})}>
            전체 보기
          </button>
          <button type="button" className="btn" aria-pressed={mine} onClick={() => setParams({ mine: 'true' })}>
            내가 쓴 글 보기
          </button>
        </div>
      </div>

      {error && <p className="field-error">{error}</p>}
      {!data && !error && <Loading />}
      {data && data.items.length === 0 && <EmptyState message={emptyMessage} />}
      {data && data.items.length > 0 && (
        <>
          <div className="ps-cards">
            {data.items.map((story) => (
              <PassStoryCard key={story.id} story={story} />
            ))}
          </div>
          <Pagination page={data.page} total={data.total} size={data.size} onChange={changePage} />
        </>
      )}

      {rankUp && (
        <RankUpModal
          rankUp={rankUp}
          onClose={() => navigate({ search: location.search }, { replace: true, state: null })}
        />
      )}
    </section>
  );
}
