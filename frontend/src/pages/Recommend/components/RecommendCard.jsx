import { Link } from 'react-router-dom';
import ScoreReason from '@/pages/Recommend/components/ScoreReason';
import { formatPercent } from '@/utils/format';

const REGISTRATION_LABELS = {
  NOT_OPEN: '접수 예정',
  OPEN: '접수 중',
  VACANCY_NOT_OPEN: '빈자리 접수 예정',
  VACANCY_OPEN: '빈자리 접수 중',
  UNKNOWN: '접수일 미정',
};
const BASIS_LABELS = {
  existing_direct_links: '학과 직접 연결',
  recommended: '전공 연관',
  same_subfield: '동계열 공통',
  faculty_aggregate: '학부 소속 학과 합산',
};
const MATCH_LABELS = { JOB_CATEGORY: '희망 직무', INTEREST_FIELD: '관심 분야' };
const NO_SCHEDULE = '일정 없음';

const monthDay = (date) => {
  const [, m, d] = date.split('-').map(Number);
  return `${m}월 ${d}일`;
};
const period = (start, end) => (start && end ? `${monthDay(start)} ~ ${monthDay(end)}` : '');

// 자격증 추천 유형: 조건 선택은 일치한 조건, 전공 맞춤은 학과 연결 근거
function recommendType(item) {
  if (item.interest_matches?.length) {
    return `${item.interest_matches.map((m) => MATCH_LABELS[m.type]).join('·')} 맞춤`;
  }
  return `전공 맞춤 · ${BASIS_LABELS[item.basis] ?? item.basis}`;
}

// 최근 합격률: 최근 연도 마지막 단계(최종) 기준
function passRate(latest) {
  const last = latest?.items.at(-1);
  if (!last || last.pass_rate_pct === null) return { value: '정보 없음' };
  return { value: `약 ${formatPercent(last.pass_rate_pct, 0)}`, note: `${latest.year} ${last.exam_stage}` };
}

function registration(schedule) {
  if (!schedule) return { value: NO_SCHEDULE };
  const vacancy = schedule.registration_status.startsWith('VACANCY');
  const dates = vacancy
    ? period(schedule.vacancy_registration_start_date, schedule.vacancy_registration_end_date)
    : period(schedule.registration_start_date, schedule.registration_end_date);
  return {
    value: dates || REGISTRATION_LABELS[schedule.registration_status],
    note: `${schedule.exam_stage} ${REGISTRATION_LABELS[schedule.registration_status]}`,
  };
}

const nearestExam = (schedule) =>
  schedule
    ? { value: monthDay(schedule.exam_start_date), note: `${schedule.exam_stage} · ${schedule.dday_label}` }
    : { value: NO_SCHEDULE };

function Tile({ label, value, note }) {
  return (
    <div className="recommend-card__tile">
      <span>{label}</span>
      <strong>{value}</strong>
      {note && <small>{note}</small>}
    </div>
  );
}

// 찜하기 책갈피 아이콘
function BookmarkIcon() {
  return (
    <svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true">
      <path d="M6 3h12v18l-6-4.5L6 21z" fill="currentColor" className="recommend-card__mark" stroke="currentColor" />
    </svg>
  );
}

// inert: 커버플로우 양옆 카드 (보이기만 하고 누를 수 없음)
export default function RecommendCard({ item, rank, scoreBasisDate, onToggleFavorite, inert = false }) {
  const { card } = item;
  const field = card.job_category_name || card.interest_field_name;
  const type = [field && `${field} 분야`, card.qualgbnm].filter(Boolean).join(' | ');

  return (
    <article className="recommend-card" inert={inert}>
      <header className="recommend-card__head">
        <div>
          <p className="recommend-card__rank">추천 {rank}위</p>
          <h2 className="recommend-card__name">{item.jmfldnm}</h2>
          <p className="recommend-card__type">{type}</p>
        </div>
        <button
          type="button"
          className={item.favorited ? 'recommend-card__favorite is-active' : 'recommend-card__favorite'}
          aria-pressed={item.favorited}
          onClick={() => onToggleFavorite(item)}
        >
          {item.favorited ? '찜 해제' : '찜하기'}
          <BookmarkIcon />
        </button>
      </header>

      {card.summary && <p className="recommend-card__summary">{card.summary}</p>}

      <div className="recommend-card__tiles">
        <Tile label="자격증 추천 유형" value={recommendType(item)} note={`추천 점수 ${item.score}점`} />
        <Tile label="최근 합격률" {...passRate(card.pass_rate_latest)} />
        <Tile label="다음 접수 기간" {...registration(card.next_registration)} />
        <Tile label="📅 가장 가까운 시험 일정" {...nearestExam(card.nearest_exam)} />
      </div>

      <details className="recommend-card__reason">
        <summary>추천 근거 보기</summary>
        <ScoreReason score={item.score} breakdown={item.score_breakdown} scoreBasisDate={scoreBasisDate} />
      </details>

      <footer className="recommend-card__actions">
        <Link className="recommend-card__detail" to={`/qualifications/${item.jmcd}`}>
          상세보기 →
        </Link>
        <Link className="recommend-card__planner" to={`/ai-planner?jmcd=${item.jmcd}`}>
          AI 플래너 생성하기
        </Link>
      </footer>
    </article>
  );
}
