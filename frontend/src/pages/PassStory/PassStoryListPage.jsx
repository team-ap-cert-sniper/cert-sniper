import RankDashboard from '@/pages/PassStory/components/RankDashboard';
import PassStoryCard from '@/components/passstory/PassStoryCard';
import RankUpModal from '@/pages/PassStory/components/RankUpModal';

export default function PassStoryListPage() {
  return (
    <section>
      <h1>저격왕</h1>
      <RankDashboard />
      <PassStoryCard />
      <RankUpModal />
    </section>
  );
}
