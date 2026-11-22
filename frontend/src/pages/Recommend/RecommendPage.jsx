import ConditionBar from '@/pages/Recommend/components/ConditionBar';
import RecommendCarousel from '@/pages/Recommend/components/RecommendCarousel';
import TrendList from '@/components/qualification/TrendList';
import PassStoryCard from '@/components/passstory/PassStoryCard';

export default function RecommendPage() {
  return (
    <section>
      <h1>자격증 추천</h1>
      <ConditionBar />
      <RecommendCarousel />
      <TrendList />
      <PassStoryCard />
    </section>
  );
}
