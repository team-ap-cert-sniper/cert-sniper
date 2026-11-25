import TrendList from '@/components/qualification/TrendList';
import TodaySchedulePanel from '@/components/planner/TodaySchedulePanel';

export default function MainPage() {
  return (
    <section>
      <h1>초기 메인</h1>
      <TrendList layout="rows" limit={5} title="인기 자격증" />
      <TodaySchedulePanel />
    </section>
  );
}
