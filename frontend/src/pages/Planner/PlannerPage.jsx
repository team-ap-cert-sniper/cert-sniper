import TargetExamDropdown from '@/pages/Planner/components/TargetExamDropdown';
import DdayBadge from '@/pages/Planner/components/DdayBadge';
import Timeline from '@/pages/Planner/components/Timeline';
import MiniCalendar from '@/components/planner/MiniCalendar';
import DailyQuote from '@/components/planner/DailyQuote';
import ScheduleModal from '@/pages/Planner/components/ScheduleModal';
import PersonalScheduleModal from '@/pages/Planner/components/PersonalScheduleModal';
import TargetExamSearchModal from '@/pages/Planner/components/TargetExamSearchModal';
import MemoModal from '@/pages/Planner/components/MemoModal';

export default function PlannerPage() {
  return (
    <section>
      <h1>플래너 다이어리</h1>
      <TargetExamDropdown />
      <DdayBadge />
      <Timeline />
      <MiniCalendar />
      <DailyQuote />
      <ScheduleModal />
      <PersonalScheduleModal />
      <TargetExamSearchModal />
      <MemoModal />
    </section>
  );
}
