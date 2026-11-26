import { RANK_LABELS } from '@/components/passstory/labels';
import '@/styles/passstory.css';

export default function RankBadge({ rank }) {
  if (!rank) return null;
  return <span className="rank-badge">{RANK_LABELS[rank] ?? rank}</span>;
}
