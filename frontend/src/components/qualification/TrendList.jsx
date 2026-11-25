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
const TREND_YEARS = 5;

function trendText(item) {
  const label = TREND_LABELS[item.trend];
  const change = item.five_year_change_percent;
  return change === null ? label : `${label} ${formatPercent(Math.abs(change))}`;
}

const subtitle = (item) => [item.interest_field_name, item.grade_level].filter(Boolean).join(' | ');
const typeName = (qualgbcd) => QUALIFICATION_TYPES.find((t) => t.value === qualgbcd)?.label;
const cardSubtitle = (item) =>
  [item.interest_field_name && `${item.interest_field_name} 분야`, typeName(item.qualgbcd)].filter(Boolean).join(' | ');

/**
 * 지금 주목받는 자격증 (최근 완결 연도 1차 실제 응시자 순위).
 * layout: list(자격증 상세 첫 화면, 구분 탭·증감) · cards(추천 페이지 가로 카드) · rows(메인 인기 자격증)
 */
export default function TrendList({ limit = 3, title = '지금 주목받는 자격증', layout = 'list' }) {
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

  const renderItem = (item) => {
    const to = `/qualifications/${item.jmcd}`;
    if (layout === 'cards') {
      return (
        <Link className="trend-card" to={to}>
          <strong>{item.jmfldnm}</strong>
          <small>{cardSubtitle(item)}</small>
          <span className="trend-card__go" aria-hidden="true">
            ›
          </span>
        </Link>
      );
    }
    if (layout === 'rows') {
      return (
        <div className="trend-row">
          <span className="trend-list__rank">{item.rank}</span>
          <span className="trend-list__name">
            <strong>{item.jmfldnm}</strong>
            <small>
              {subtitle(item)} · 응시자 {formatNumber(item.examinees)}명
            </small>
          </span>
          <Link className="trend-row__button" to={to}>
            해당 자격증 상세보기
          </Link>
        </div>
      );
    }
    return (
      <Link className="trend-list__item" to={to}>
        <span className="trend-list__rank">{item.rank}</span>
        <span className="trend-list__name">
          <strong>{item.jmfldnm}</strong>
          <small>{subtitle(item)}</small>
        </span>
        <span className="trend-list__count">응시자 {formatNumber(item.examinees)}명</span>
        <span className={`trend-list__badge trend-list__badge--${item.trend.toLowerCase()}`}>{trendText(item)}</span>
      </Link>
    );
  };

  const renderBody = () => {
    if (error) return <EmptyState message={error} />;
    if (!data) return <Loading />;
    if (data.items.length === 0) return <EmptyState message="표시할 자격증이 없습니다." />;
    return (
      <ol className={`trend-list__items trend-list__items--${layout}`}>
        {data.items.map((item) => (
          <li key={item.jmcd}>{renderItem(item)}</li>
        ))}
      </ol>
    );
  };

  return (
    <section className="trend-list">
      <div className="trend-list__head">
        <h2>{title}</h2>
        {layout === 'list' && (
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
        )}
        <Link className="trend-list__more" to="/qualifications">
          전체 자격증 보기
        </Link>
      </div>
      {data?.year && layout !== 'cards' && (
        <p className="trend-list__basis">
          {data.basis} 기준{layout === 'list' && ` · ${data.year - TREND_YEARS + 1}년 대비 증감`}
        </p>
      )}
      {renderBody()}
    </section>
  );
}
