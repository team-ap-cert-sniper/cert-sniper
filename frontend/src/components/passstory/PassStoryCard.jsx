import { Link } from 'react-router-dom';
import RankBadge from '@/components/passstory/RankBadge';
import { ATTEMPT_LABELS, PREP_PERIOD_LABELS, STATUS_LABELS } from '@/components/passstory/labels';
import { formatDate } from '@/utils/date';
import '@/styles/passstory.css';

// story = GET /pass-stories 응답 items[] 한 개 (snake_case)
export default function PassStoryCard({ story }) {
  return (
    <article className="pass-story-card">
      <Link to={`/pass-stories/${story.id}`}>
        <div className="pass-story-card__top">
          <span className="pass-story-card__author">
            {story.nickname} · <RankBadge rank={story.current_rank} />
          </span>
          {story.status !== 'PUBLISHED' && (
            <span className="ps-chip ps-chip--danger">{STATUS_LABELS[story.status]}</span>
          )}
        </div>
        <h3>{story.jmfldnm}</h3>
        <p className="pass-story-card__preview">{story.body_preview}</p>
        <div className="pass-story-card__bottom">
          <span className="pass-story-card__tags">
            {story.prep_period && <span className="ps-chip">준비 기간 {PREP_PERIOD_LABELS[story.prep_period]}</span>}
            {story.attempt_band && <span className="ps-chip">응시 {ATTEMPT_LABELS[story.attempt_band]}</span>}
          </span>
          <span className="pass-story-card__date">{formatDate(story.created_at)}</span>
        </div>
      </Link>
    </article>
  );
}
