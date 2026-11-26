import { useEffect } from 'react';
import TodaySchedulePanel from '@/components/planner/TodaySchedulePanel';
import TrendList from '@/components/qualification/TrendList';
import useScrollScene from '@/hooks/useScrollScene';
import ScrollShowcase from '@/pages/Main/components/ScrollShowcase';
import '@/styles/main.css';

// 메인 페이지 (VIEW-01, 담당: 유다연)
// ① 히어로(배너 + 사이드바) → ② 스크롤 애니메이션(ScrollShowcase) → ③ 인기 자격증
export default function MainPage() {
  const heroRef = useScrollScene(1);
  const popularRef = useScrollScene();

  // 메인에 들어오면 항상 맨 위(히어로)부터 보이게.
  // 브라우저·라우터가 이전 스크롤 위치를 기억해서 중간부터 열리는 것을 막는다.
  useEffect(() => {
    const previous = window.history.scrollRestoration;
    window.history.scrollRestoration = 'manual';
    window.scrollTo(0, 0);
    return () => {
      window.history.scrollRestoration = previous;
    };
  }, []);

  return (
    <div className="main-page">
      {/* ① 히어로 */}
      <section ref={heroRef} className="main-hero">
        {/* TODO 히어로 디자인 확정 전 임시 배너 */}
        <div className="main-hero__banner">
          <p>
            저격해라 자격증 추천부터
            <br />
            합격 준비까지 저격해 탕탕
          </p>
        </div>
        {/* 작은 캘린더 · 사자성어 · 오늘의 일정 (박준성님 부품, 파일 수정 없이 메인에서만 배치·순서 조정) */}
        <aside className="main-hero__side">
          <TodaySchedulePanel />
        </aside>
      </section>

      {/* ② 기능 소개 스크롤 애니메이션 */}
      <div className="main-showcase">
        <ScrollShowcase />
      </div>

      {/* ③ 인기 자격증 (이은아님 부품, 수정 없이 사용) */}
      <section ref={popularRef} className="main-popular">
        <TrendList layout="rows" limit={5} title="인기 자격증" />
      </section>
    </div>
  );
}