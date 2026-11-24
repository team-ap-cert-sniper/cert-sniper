import { useEffect, useState } from 'react';
import '@/styles/scroll-progress.css';

// 헤더 바로 밑 스크롤 진행 바 (담당: 유다연)
// 스크롤할 내용이 없는 짧은 페이지에서는 그리지 않는다.
export default function ScrollProgressBar() {
  const [progress, setProgress] = useState(0);
  const [scrollable, setScrollable] = useState(false);

  useEffect(() => {
    let frame = 0;

    const update = () => {
      frame = 0;
      const max = document.documentElement.scrollHeight - window.innerHeight;
      setScrollable(max > 1);
      setProgress(max > 1 ? Math.min(window.scrollY / max, 1) : 0);
    };

    // 스크롤 이벤트가 연달아 와도 한 화면(프레임)에 한 번만 계산
    const schedule = () => {
      if (!frame) frame = requestAnimationFrame(update);
    };

    // 페이지 이동이나 데이터 로딩으로 내용 길이가 바뀌어도 다시 계산
    const observer = new ResizeObserver(schedule);
    observer.observe(document.body);
    window.addEventListener('scroll', schedule, { passive: true });
    window.addEventListener('resize', schedule);
    schedule();

    return () => {
      observer.disconnect();
      window.removeEventListener('scroll', schedule);
      window.removeEventListener('resize', schedule);
      cancelAnimationFrame(frame);
    };
  }, []);

  if (!scrollable) return null;

  return (
    <div className="scroll-progress" aria-hidden="true">
      <div className="scroll-progress__fill" style={{ transform: `scaleX(${progress})` }} />
    </div>
  );
}
