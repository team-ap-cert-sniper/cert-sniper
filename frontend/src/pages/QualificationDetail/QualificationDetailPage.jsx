import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getQualificationDetail } from '@/api/qualificationApi';
import EmptyState from '@/components/common/EmptyState';
import Loading from '@/components/common/Loading';
import { errorMessage } from '@/components/passstory/utils';
import TrendList from '@/components/qualification/TrendList';
import EligibilityInfo from '@/pages/QualificationDetail/components/EligibilityInfo';
import ExamScheduleTable from '@/pages/QualificationDetail/components/ExamScheduleTable';
import PassRateChart from '@/pages/QualificationDetail/components/PassRateChart';
import PassRateTable from '@/pages/QualificationDetail/components/PassRateTable';
import QualificationBrowser from '@/pages/QualificationDetail/components/QualificationBrowser';
import QualificationInfo from '@/pages/QualificationDetail/components/QualificationInfo';
import QualificationSearchModal from '@/pages/QualificationDetail/components/QualificationSearchModal';
import '@/styles/qualification-detail.css';

const TREND_LIMIT = 5;

function Detail({ detail }) {
  return (
    <>
      <QualificationInfo qualification={detail.qualification} />
      <ExamScheduleTable name={detail.qualification.jmfldnm} schedules={detail.schedules} />
      {detail.pass_rates.length === 0 ? (
        <section className="qualification-card">
          <p className="qualification-empty">합격률 통계 정보가 없습니다.</p>
        </section>
      ) : (
        <>
          <PassRateChart passRates={detail.pass_rates} years={detail.pass_rate_years} />
          <PassRateTable passRates={detail.pass_rates} years={detail.pass_rate_years} />
        </>
      )}
      <EligibilityInfo qualification={detail.qualification} eligibility={detail.eligibility} />
    </>
  );
}

// 자격증 상세: 첫 화면은 검색창 + 탐색 + 트렌드, 종목을 고르면 그 자격증 상세
export default function QualificationDetailPage() {
  const { jmcd } = useParams();
  const navigate = useNavigate();
  const [loaded, setLoaded] = useState({ jmcd: null, detail: null, error: '' });
  const [keyword, setKeyword] = useState('');
  const [searchOpen, setSearchOpen] = useState(false);

  useEffect(() => {
    if (!jmcd) return undefined;
    let ignore = false;
    getQualificationDetail(jmcd)
      .then((detail) => {
        if (!ignore) setLoaded({ jmcd, detail, error: '' });
      })
      .catch((e) => {
        if (!ignore) setLoaded({ jmcd, detail: null, error: errorMessage(e) });
      });
    return () => {
      ignore = true;
    };
  }, [jmcd]);

  const open = (code) => {
    setSearchOpen(false);
    navigate(`/qualifications/${code}`);
  };

  const renderDetail = () => {
    if (loaded.jmcd !== jmcd) return <Loading />;
    if (loaded.error) return <EmptyState message={loaded.error} />;
    return <Detail key={jmcd} detail={loaded.detail} />;
  };

  return (
    <section className="container qualification-page">
      <form
        className="qualification-search"
        onSubmit={(e) => {
          e.preventDefault();
          setSearchOpen(true);
        }}
      >
        <input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="종목명을 검색하세요." />
        <button type="submit" className="qualification-button">
          검색하기
        </button>
      </form>
      {searchOpen && (
        <QualificationSearchModal initialQuery={keyword} onSelect={open} onClose={() => setSearchOpen(false)} />
      )}

      <QualificationBrowser selectedJmcd={loaded.detail?.jmcd} onSelect={open} />

      {jmcd ? renderDetail() : <TrendList limit={TREND_LIMIT} />}
    </section>
  );
}
