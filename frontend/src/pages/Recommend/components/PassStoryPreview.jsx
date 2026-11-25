import { Link } from 'react-router-dom';
import RankBadge from '@/components/passstory/RankBadge';
import { PREP_PERIOD_LABELS } from '@/components/passstory/labels';
import { formatDate } from '@/utils/date';

const PREVIEW_LENGTH = 50;

const preview = (text = '') => (text.length > PREVIEW_LENGTH ? `${text.slice(0, PREVIEW_LENGTH)}…` : text);

// 합격수기 최신순 미리보기 가로 목록. stories = GET /pass-stories items[]
export default function PassStoryPreview({ stories }) {
  return (
    <ul className="story-preview">
      {stories.map((story) => (
        <li key={story.id}>
          <Link className="story-preview__item" to={`/pass-stories/${story.id}`}>
            <span className="story-preview__tags">
              <span>{story.jmfldnm}</span>
              {story.prep_period && <span>{PREP_PERIOD_LABELS[story.prep_period]}</span>}
            </span>
            <span className="story-preview__writer">
              {story.nickname} <RankBadge rank={story.current_rank} />
            </span>
            <span className="story-preview__body">{preview(story.body_preview)}</span>
            <span className="story-preview__date">{formatDate(story.created_at)}</span>
            <span className="story-preview__go" aria-hidden="true">
              ›
            </span>
          </Link>
        </li>
      ))}
    </ul>
  );
}
