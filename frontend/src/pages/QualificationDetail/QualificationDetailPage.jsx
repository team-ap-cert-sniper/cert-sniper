import QualificationSearchModal from '@/pages/QualificationDetail/components/QualificationSearchModal';
import QualificationBrowser from '@/pages/QualificationDetail/components/QualificationBrowser';
import TrendList from '@/components/qualification/TrendList';
import QualificationInfo from '@/pages/QualificationDetail/components/QualificationInfo';
import EligibilityInfo from '@/pages/QualificationDetail/components/EligibilityInfo';
import PassRateChart from '@/pages/QualificationDetail/components/PassRateChart';
import PassRateTable from '@/pages/QualificationDetail/components/PassRateTable';
import ExamScheduleTable from '@/pages/QualificationDetail/components/ExamScheduleTable';

export default function QualificationDetailPage() {
  return (
    <section>
      <h1>자격증 상세</h1>
      <QualificationSearchModal />
      <QualificationBrowser />
      <TrendList />
      <QualificationInfo />
      <EligibilityInfo />
      <PassRateChart />
      <PassRateTable />
      <ExamScheduleTable />
    </section>
  );
}
