import { useState } from 'react';
import MiniCalendar from '@/components/planner/MiniCalendar';
import DailyQuote from '@/components/planner/DailyQuote';
import { formatDate, toDateString } from '@/utils/date';

/** 오늘의 일정 패널: 작은 캘린더 · 선택 날짜 일정 · 사자성어 (메인·플래너 공통) CF-00-04 */
export default function TodaySchedulePanel() {
  const [selectedDate, setSelectedDate] = useState(() => toDateString(new Date()));

  return (
    <aside className="today-panel">
      <MiniCalendar selectedDate={selectedDate} onSelectDate={setSelectedDate} />
      <p className="today-panel__date">{formatDate(selectedDate)} 일정</p>
      {/* 일정 목록: 로그인·GET /api/v1/planner/today 구현 후 연결 */}
      <DailyQuote />
    </aside>
  );
}
