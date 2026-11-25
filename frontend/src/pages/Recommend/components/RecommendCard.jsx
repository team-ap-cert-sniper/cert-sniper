import { Link } from 'react-router-dom';
import ScoreReason from '@/pages/Recommend/components/ScoreReason';
import { formatDate } from '@/utils/date';
import { formatPercent } from '@/utils/format';

const REGISTRATION_LABELS = {
  NOT_OPEN: '접수 예정',
  OPEN: '접수 중',
  VACANCY_NOT_OPEN: '빈자리 접수 예정',
  VACANCY_OPEN: '빈자리 접수 중',
  UNKNOWN: '접수일 미정',
};
const NOT_PROVIDED = '미제공';

const period = (start, end) => (start && end ? `${formatDate(start)} ~ ${formatDate(end)}` : '');

function passRateText(latest) {
  if (!latest) return NOT_PROVIDED;
  const stages = latest.items.map((r) => `${r.exam_stage} ${formatPercent(r.pass_rate_pct)}`).join(' · ');
  return `${latest.year}년 ${stages}`;
}

function registrationText(schedule) {
  if (!schedule) return NOT_PROVIDED;
  const vacancy = schedule.registration_status.startsWith('VACANCY');
  const dates = vacancy
    ? period(schedule.vacancy_registration_start_date, schedule.vacancy_registration_end_date)
    : period(schedule.registration_start_date, schedule.registration_end_date);
  return [schedule.exam_round, schedule.exam_stage, REGISTRATION_LABELS[schedule.registration_status], dates]
    .filter(Boolean)
    .join(' · ');
}

function examText(schedule) {
  if (!schedule) return NOT_PROVIDED;
  return `${formatDate(schedule.exam_start_date)} ${schedule.exam_stage} (${schedule.dday_label})`;
}

export default function RecommendCard({ item, rank, scoreBasisDate, onToggleFavorite }) {
  const { card } = item;
  const type = [card.qualgbnm, card.grade_level].filter(Boolean).join(' · ');

  return (
    <article className="recommend-card">
      <header className="recommend-card__head">
        <div>
          <p className="recommend-card__rank">추천 {rank}위</p>
          <h2 className="recommend-card__name">{item.jmfldnm}</h2>
          <p className="recommend-card__type">
            {type}
            {(card.job_category_name || card.interest_field_name) &&
              ` | ${card.job_category_name || card.interest_field_name}`}
          </p>
        </div>
        <button
          type="button"
          className={item.favorited ? 'recommend-card__favorite is-active' : 'recommend-card__favorite'}
          aria-pressed={item.favorited}
          onClick={() => onToggleFavorite(item)}
        >
          {item.favorited ? '찜 해제' : '찜하기'}
        </button>
      </header>

      {card.summary && <p className="recommend-card__summary">{card.summary}</p>}

      <dl className="recommend-card__facts">
        <dt>최근 합격률</dt>
        <dd>{passRateText(card.pass_rate_latest)}</dd>
        <dt>다음 접수</dt>
        <dd>{registrationText(card.next_registration)}</dd>
        <dt>가까운 시험</dt>
        <dd>{examText(card.nearest_exam)}</dd>
      </dl>

      <ScoreReason score={item.score} breakdown={item.score_breakdown} scoreBasisDate={scoreBasisDate} />

      <footer className="recommend-card__actions">
        <Link className="btn" to={`/qualifications/${item.jmcd}`}>
          상세보기
        </Link>
        <Link className="btn btn--primary" to={`/ai-planner?jmcd=${item.jmcd}`}>
          AI 플래너 생성하기
        </Link>
      </footer>
    </article>
  );
}
