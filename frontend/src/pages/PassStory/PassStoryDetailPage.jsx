import RankBadge from '@/components/passstory/RankBadge';
import HiddenNotice from '@/components/passstory/HiddenNotice';
import ReportModal from '@/pages/PassStory/components/ReportModal';

export default function PassStoryDetailPage() {
  return (
    <section>
      <h1>합격수기 상세</h1>
      <RankBadge />
      <HiddenNotice />
      <ReportModal />
    </section>
  );
}
