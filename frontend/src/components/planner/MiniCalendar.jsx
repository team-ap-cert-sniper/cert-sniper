import { useState } from 'react';
import { toDateString } from '@/utils/date';
import '@/styles/mini-calendar.css';

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];

/** 그 달의 칸 목록: 1일 앞의 빈칸은 null, 날짜 칸은 'YYYY-MM-DD' */
function buildMonthCells(year, month) {
  const firstWeekday = new Date(year, month, 1).getDay();
  const lastDate = new Date(year, month + 1, 0).getDate();
  const cells = Array(firstWeekday).fill(null);
  for (let day = 1; day <= lastDate; day += 1) {
    cells.push(toDateString(new Date(year, month, day)));
  }
  return cells;
}

function toYearMonth(dateString) {
  const [year, month] = dateString.split('-').map(Number);
  return { year, month: month - 1 };
}

/**
 * 작은 캘린더 (메인 오늘의 일정 패널 · 플래너 사이드바 공통) CF-04-03
 * selectedDate: 선택 날짜 'YYYY-MM-DD' (없으면 오늘)
 * onSelectDate: 날짜를 누르면 'YYYY-MM-DD'로 호출
 * markedDates: 일정이 있는 날짜 목록 (점 표시)
 */
export default function MiniCalendar({ selectedDate, onSelectDate, markedDates = [] }) {
  const [today] = useState(() => toDateString(new Date()));
  const selected = selectedDate ?? today;
  const [view, setView] = useState(() => toYearMonth(selected));

  const cells = buildMonthCells(view.year, view.month);
  const marked = new Set(markedDates);

  const moveMonth = (diff) => {
    setView(({ year, month }) => {
      const moved = new Date(year, month + diff, 1);
      return { year: moved.getFullYear(), month: moved.getMonth() };
    });
  };

  const goToday = () => {
    setView(toYearMonth(today));
    onSelectDate?.(today);
  };

  const dayClassName = (date, index) =>
    [
      'mini-calendar__day',
      index % 7 === 0 && 'is-sunday',
      date === today && 'is-today',
      date === selected && 'is-selected',
      marked.has(date) && 'has-schedule',
    ]
      .filter(Boolean)
      .join(' ');

  return (
    <div className="mini-calendar">
      <div className="mini-calendar__header">
        <button type="button" className="mini-calendar__nav" onClick={() => moveMonth(-1)} aria-label="이전 달">
          ‹
        </button>
        <span className="mini-calendar__title">
          {view.year}년 {view.month + 1}월
        </span>
        <button type="button" className="mini-calendar__nav" onClick={() => moveMonth(1)} aria-label="다음 달">
          ›
        </button>
      </div>

      <div className="mini-calendar__grid">
        {WEEKDAYS.map((weekday) => (
          <span key={weekday} className="mini-calendar__weekday">
            {weekday}
          </span>
        ))}
        {cells.map((date, index) =>
          date ? (
            <button key={date} type="button" className={dayClassName(date, index)} onClick={() => onSelectDate?.(date)}>
              {Number(date.slice(8))}
            </button>
          ) : (
            <span key={`empty-${index}`} />
          ),
        )}
      </div>

      <button type="button" className="mini-calendar__today" onClick={goToday}>
        오늘
      </button>
    </div>
  );
}
