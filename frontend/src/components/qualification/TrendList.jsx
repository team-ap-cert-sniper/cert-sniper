import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getTrends } from '@/api/qualificationApi';
import EmptyState from '@/components/common/EmptyState';
import Loading from '@/components/common/Loading';
import { errorMessage } from '@/components/passstory/utils';
import { QUALIFICATION_TYPES } from '@/constants/qualification';
import { formatNumber, formatPercent } from '@/utils/format';
import '@/styles/trend.css';

const TREND_LABELS = { UP: '증가', DOWN: '감소', STEADY: '유지', NEW_OR_SHORT_HISTORY: '신규' };

function trendText(item) {
  const label = TREND_LABELS[item.trend];
  const change = item.five_year_change_percent;
  return change === null ? label : `${label} ${formatPercent(Math.abs(change))}`;
}

// 지금 주목받는 자격증: 최근 완결 연도 1차 응시자 순위 (추천 페이지 1~3위, 메인 공용)
export default function TrendList({ limit = 3, title = '지금 주목받는 자격증' }) {
  const [qualgbcd, setQualgbcd] = useState('T');
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let ignore = false;
    getTrends({ qualgbcd, limit })
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
  }, [qualgbcd, limit]);

  const renderBody = () => {
    if (error) return <EmptyState message={error} />;
    if (!data) return <Loading />;
    if (data.items.length === 0) return <EmptyState message="표시할 트렌드가 없습니다." />;
    return (
      <ol className="trend-list__items">
        {data.items.map((item) => (
          <li key={item.jmcd}>
            <Link className="trend-list__item" to={`/qualifications/${item.jmcd}`}>
              <span className="trend-list__rank">{item.rank}</span>
              <span className="trend-list__name">
                <strong>{item.jmfldnm}</strong>
                <small>{[item.grade_level, item.interest_field_name].filter(Boolean).join(' · ')}</small>
              </span>
              <span className="trend-list__count">응시자 {formatNumber(item.examinees)}명</span>
              <span className={`trend-list__badge trend-list__badge--${item.trend.toLowerCase()}`}>
                {trendText(item)}
              </span>
            </Link>
          </li>
        ))}
      </ol>
    );
  };

  return (
    <section className="trend-list">
      <div className="trend-list__head">
        <h2>{title}</h2>
        <div className="trend-list__tabs" role="group" aria-label="자격 구분">
          {QUALIFICATION_TYPES.map((type) => (
            <button
              key={type.value}
              type="button"
              className={qualgbcd === type.value ? 'chip chip--selected' : 'chip'}
              aria-pressed={qualgbcd === type.value}
              onClick={() => setQualgbcd(type.value)}
            >
              {type.label}
            </button>
          ))}
        </div>
        <Link className="trend-list__more" to="/qualifications">
          전체 자격증 보기
        </Link>
      </div>
      {data?.basis && <p className="trend-list__basis">{data.basis} 기준 · 5년 전 대비 증감</p>}
      {renderBody()}
    </section>
  );
}
