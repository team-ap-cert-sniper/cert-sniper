import { formatDate } from '@/utils/date';

// 추천 근거: 항목별 점수와 합계, 점수 기준일
export default function ScoreReason({ score, breakdown, scoreBasisDate }) {
  return (
    <div className="score-reason">
      <p className="score-reason__total">
        추천 점수 <strong>{score}점</strong>
        <span className="score-reason__date">기준일 {formatDate(scoreBasisDate)}</span>
      </p>
      <ul className="score-reason__list">
        {breakdown.map((part) => (
          <li key={part.factor}>
            <span>{part.reason}</span>
            <span className={part.points < 0 ? 'score-reason__points is-minus' : 'score-reason__points'}>
              {part.points > 0 ? `+${part.points}` : part.points}
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}
