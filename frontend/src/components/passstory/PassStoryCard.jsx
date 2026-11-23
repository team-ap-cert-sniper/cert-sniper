import { Link } from 'react-router-dom';
import RankBadge from '@/components/passstory/RankBadge';
import { ATTEMPT_LABELS, PREP_PERIOD_LABELS, STATUS_LABELS } from '@/components/passstory/labels';
import { formatDate } from '@/utils/date';

// story = GET /pass-stories 응답 items[] 한 개 (snake_case)
export default function PassStoryCard({ story }) {
  return (
    <article className="pass-story-card">
      <Link to={`/pass-stories/${story.id}`}>
        <h3>{story.jmfldnm}</h3>
        <p>
          {story.nickname} <RankBadge rank={story.current_rank} />
        </p>
        <p>{story.body_preview}</p>
        <p>
          {story.prep_period && <span>준비 기간 {PREP_PERIOD_LABELS[story.prep_period]} </span>}
          {story.attempt_band && <span>응시 {ATTEMPT_LABELS[story.attempt_band]}</span>}
        </p>
        <p>
          {formatDate(story.created_at)}
          {story.status !== 'PUBLISHED' && <strong> [{STATUS_LABELS[story.status]}]</strong>}
        </p>
      </Link>
    </article>
  );
}
