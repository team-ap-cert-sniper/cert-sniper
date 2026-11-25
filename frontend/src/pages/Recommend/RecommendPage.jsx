import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { addFavorite, removeFavorite } from '@/api/favoriteApi';
import { getPassStories } from '@/api/passStoryApi';
import { recommend, recommendByCondition } from '@/api/recommendationApi';
import { getMyProfile } from '@/api/userApi';
import AlertModal from '@/components/common/AlertModal';
import EmptyState from '@/components/common/EmptyState';
import Loading from '@/components/common/Loading';
import { errorMessage } from '@/components/passstory/utils';
import TrendList from '@/components/qualification/TrendList';
import ConditionBar from '@/pages/Recommend/components/ConditionBar';
import PassStoryPreview from '@/pages/Recommend/components/PassStoryPreview';
import RecommendCarousel from '@/pages/Recommend/components/RecommendCarousel';
import '@/styles/recommend.css';

const STORY_COUNT = 3;

// 회원정보 보유 자격 중 종목코드가 있는 것 (직접 입력한 자격증명은 제외)
const ownedJmcdsOf = (profile) => (profile?.owned_certificates ?? []).map((c) => c.jmcd).filter(Boolean);

// 결과가 0개일 때 안내 (전공 맞춤: 학교 정보 없음 · 학과 연결 없음 / 조건 선택: 조건 불일치)
function emptyMessage(result) {
  if (result.needs_school_info) {
    return '학교 정보가 없어 전공 맞춤 추천을 할 수 없습니다. 관심 분야·희망 직무를 골라 주세요.';
  }
  if (result.mode === 'CONDITION') return '조건에 맞는 자격증이 없습니다. 조건을 바꿔 다시 찾아보세요.';
  return '이 전공과 연결된 자격증이 없습니다. 관심 분야·희망 직무로 찾아보세요.';
}

export default function RecommendPage() {
  const [profile, setProfile] = useState(null);
  const [majorResult, setMajorResult] = useState(null);
  const [result, setResult] = useState(null);
  const [applied, setApplied] = useState(null);
  const [stories, setStories] = useState([]);
  const [loadError, setLoadError] = useState('');
  const [conditionError, setConditionError] = useState('');
  const [searching, setSearching] = useState(false);
  const [panelOpen, setPanelOpen] = useState(false);
  const [alert, setAlert] = useState('');

  // 진입·새로고침: 회원정보(학교·전공·학년·보유 자격)로 전공 맞춤 추천
  useEffect(() => {
    let ignore = false;
    getMyProfile()
      .then(async (me) => {
        const data = await recommend({
          university_id: me.university_id,
          college_id: me.major_id ? null : me.college_id,
          major_id: me.major_id,
          grade: me.grade,
          owned_jmcds: ownedJmcdsOf(me),
        });
        if (ignore) return;
        setProfile(me);
        setMajorResult(data);
        setResult(data);
        setPanelOpen(data.items.length === 0);
      })
      .catch((e) => {
        if (!ignore) setLoadError(errorMessage(e));
      });
    getPassStories({ page: 0, size: STORY_COUNT })
      .then((data) => {
        if (!ignore) setStories(data.items);
      })
      .catch(() => {});
    return () => {
      ignore = true;
    };
  }, []);

  // 조건 선택 추천 (조건은 저장하지 않음, 실패하면 기존 결과 유지)
  const searchByCondition = async ({ labels, ...conditions }) => {
    setSearching(true);
    try {
      const data = await recommendByCondition({ ...conditions, owned_jmcds: ownedJmcdsOf(profile) });
      setResult(data);
      setApplied(labels);
      setConditionError('');
    } catch (e) {
      setConditionError(errorMessage(e));
    } finally {
      setSearching(false);
    }
  };

  // 원래 전공 맞춤 추천으로 되돌리기 (다시 부르지 않고 처음 결과 사용)
  const resetToMajor = () => {
    setResult(majorResult);
    setApplied(null);
    setConditionError('');
  };

  // 찜 상태는 지금 결과와 처음 결과 모두에 반영 (되돌려도 맞게)
  const setFavorited = (jmcd, favorited) => {
    const update = (prev) =>
      prev && { ...prev, items: prev.items.map((item) => (item.jmcd === jmcd ? { ...item, favorited } : item)) };
    setResult(update);
    setMajorResult(update);
  };

  // 찜 등록·해제: 화면 먼저 바꾸고 실패하면 되돌림
  const toggleFavorite = async (item) => {
    setFavorited(item.jmcd, !item.favorited);
    try {
      await (item.favorited ? removeFavorite(item.jmcd) : addFavorite(item.jmcd));
    } catch (e) {
      setFavorited(item.jmcd, item.favorited);
      setAlert(errorMessage(e));
    }
  };

  return (
    <section className="recommend">
      {loadError && <EmptyState message={loadError} />}
      {!loadError && !result && <Loading />}
      {result && (
        <>
          <ConditionBar
            profile={profile}
            applied={applied}
            open={panelOpen}
            onToggle={() => setPanelOpen((open) => !open)}
            onSearch={searchByCondition}
            onReset={applied ? resetToMajor : null}
            searching={searching}
            error={conditionError}
          />
          {result.items.length === 0 ? (
            <EmptyState message={emptyMessage(result)} />
          ) : (
            <RecommendCarousel
              key={result.items.map((item) => item.jmcd).join()}
              items={result.items}
              scoreBasisDate={result.score_basis_date}
              onToggleFavorite={toggleFavorite}
            />
          )}
        </>
      )}

      <div className="recommend-box">
        <TrendList layout="cards" />
      </div>

      <section className="recommend-box recommend__section">
        <div className="recommend__section-head">
          <h2>합격수기</h2>
          <Link to="/pass-stories">합격수기 보러 가기</Link>
        </div>
        {stories.length === 0 ? (
          <EmptyState message="아직 등록된 합격수기가 없습니다." />
        ) : (
          <PassStoryPreview stories={stories} />
        )}
        <p className="recommend__note">자격증 합격 수기 최신순으로 표시 (내용 50자 이하 중략)</p>
      </section>

      {alert && <AlertModal message={alert} onClose={() => setAlert('')} />}
    </section>
  );
}
