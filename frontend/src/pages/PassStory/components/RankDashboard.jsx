import { useEffect, useState } from 'react';
import { getMyRank } from '@/api/rankApi';
import { RANK_LABELS } from '@/components/passstory/labels';
import { errorMessage } from '@/components/passstory/utils';

// 등급 기준: 서로 다른 합격 자격증 수 (RankService와 같은 값)
const LEVELS = [
  { rank: 'TRAINEE', rule: '가입 시 자동 부여' },
  { rank: 'APPRENTICE', rule: '합격 자격증 1개' },
  { rank: 'MARKSMAN', rule: '합격 자격증 3개' },
  { rank: 'LEGEND', rule: '합격 자격증 5개' },
];

const ICON_PATHS = {
  TRAINEE: 'M12 2l2.5 5 5.5.8-4 3.9.9 5.5L12 14.6 7.1 17.2l.9-5.5-4-3.9 5.5-.8z',
  APPRENTICE: 'M12 3a9 9 0 1 0 9 9M12 7a5 5 0 1 0 5 5M12 11a1 1 0 1 0 1 1M12 12l8-8',
  MARKSMAN: 'M13 2L4 14h7l-1 8 9-12h-7z',
  LEGEND: 'M12 3l1.8 4.6L18 9l-4.2 1.4L12 15l-1.8-4.6L6 9l4.2-1.4zM19 15l.8 2 2 .8-2 .8-.8 2-.8-2-2-.8 2-.8z',
};

function RankIcon({ rank, size = 24 }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d={ICON_PATHS[rank]} />
    </svg>
  );
}

export default function RankDashboard() {
  const [rank, setRank] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getMyRank()
      .then(setRank)
      .catch((e) => setError(errorMessage(e)));
  }, []);

  if (error) return <p className="field-error">{error}</p>;
  if (!rank) return null;

  const level = LEVELS.findIndex((l) => l.rank === rank.current_rank) + 1;
  const goal = rank.valid_cert_count + (rank.remaining_for_next ?? 0);
  const percent = rank.next_rank ? Math.round((rank.valid_cert_count / goal) * 100) : 100;

  return (
    <section className="rank-dashboard ps-box" aria-labelledby="rank-dashboard-title">
      <h2 id="rank-dashboard-title" className="rank-dashboard__title">
        나의 저격 등급 대시보드
      </h2>
      <div className="rank-dashboard__grid">
        <div className="rank-dashboard__current">
          <span className="rank-dashboard__icon">
            <RankIcon rank={rank.current_rank} size={30} />
          </span>
          <div className="rank-dashboard__info">
            <p className="rank-dashboard__name">
              <span className="rank-dashboard__lv">Lv.{level}</span>
              {RANK_LABELS[rank.current_rank]}
            </p>
            <p>
              합격 자격증 {rank.valid_cert_count}개 ·{' '}
              {rank.next_rank
                ? `다음 등급 ${RANK_LABELS[rank.next_rank]}까지 ${rank.remaining_for_next}개`
                : '최고 등급입니다'}
            </p>
            <div
              className="rank-dashboard__bar"
              role="progressbar"
              aria-label="다음 등급 진척도"
              aria-valuenow={percent}
              aria-valuemin={0}
              aria-valuemax={100}
            >
              <span style={{ width: `${percent}%` }} />
            </div>
          </div>
        </div>
        {LEVELS.map((l, i) => {
          const current = l.rank === rank.current_rank;
          return (
            <div
              key={l.rank}
              className={`rank-dashboard__level${current ? ' rank-dashboard__level--current' : ''}`}
              aria-current={current ? 'true' : undefined}
            >
              {current && <span className="rank-dashboard__now">현재</span>}
              <RankIcon rank={l.rank} />
              <span>Lv.{i + 1}</span>
              <strong>{RANK_LABELS[l.rank]}</strong>
              <span>{l.rule}</span>
            </div>
          );
        })}
      </div>
    </section>
  );
}
