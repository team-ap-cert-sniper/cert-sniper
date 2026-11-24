import { useEffect, useState } from 'react';

// 아래로 스크롤하면 true(숨김), 위로 스크롤하면 false(보임)
// offset: 페이지 맨 위에서 이 높이까지는 항상 보이게 (헤더 높이)
export default function useHideOnScroll(offset = 84) {
  const [hidden, setHidden] = useState(false);

  useEffect(() => {
    let lastY = window.scrollY;
    let frame = 0;

    const update = () => {
      frame = 0;
      const y = Math.max(window.scrollY, 0);
      // 손 떨림처럼 아주 조금 움직인 건 무시
      if (Math.abs(y - lastY) < 5) return;
      setHidden(y > lastY && y > offset);
      lastY = y;
    };

    const schedule = () => {
      if (!frame) frame = requestAnimationFrame(update);
    };

    window.addEventListener('scroll', schedule, { passive: true });

    return () => {
      window.removeEventListener('scroll', schedule);
      cancelAnimationFrame(frame);
    };
  }, [offset]);

  return hidden;
}
