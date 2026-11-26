import { useEffect, useRef } from 'react';

const clamp01 = (value) => Math.min(Math.max(value, 0), 1);
// 처음엔 빠르고 끝에서 부드럽게 멈추는 곡선 (ease-out)
const easeOut = (value) => 1 - (1 - value) ** 3;

/**
 * 스크롤 장면 진행도를 CSS 변수로 넣어 준다. (React 다시 그리기 없이 style만 바꿈)
 * 진행도는 ease-out 곡선을 적용한 값이다.
 *  --enter: 요소 윗부분이 화면 맨 아래 → 맨 위로 올라오는 동안 0 → 1 (등장)
 *  --exit : 요소 아랫부분이 exitStart(화면 높이 배수) → 그보다 한 화면 위로 가는 동안 0 → 1 (퇴장)
 * exitStart 2: 높이 200vh 이상인 장면(sticky)용, 1: 히어로처럼 화면 한 장 이하인 요소용
 */
export default function useScrollScene(exitStart = 2) {
  const ref = useRef(null);

  useEffect(() => {
    const element = ref.current;
    if (!element) return undefined;
    let frame = 0;

    const update = () => {
      frame = 0;
      const viewport = window.innerHeight;
      const { top, bottom } = element.getBoundingClientRect();
      element.style.setProperty('--enter', easeOut(clamp01((viewport - top) / viewport)).toFixed(3));
      element.style.setProperty('--exit', easeOut(clamp01((viewport * exitStart - bottom) / viewport)).toFixed(3));
    };

    // 스크롤 이벤트가 연달아 와도 한 화면(프레임)에 한 번만 계산
    const schedule = () => {
      if (!frame) frame = requestAnimationFrame(update);
    };

    window.addEventListener('scroll', schedule, { passive: true });
    window.addEventListener('resize', schedule);
    schedule();

    return () => {
      window.removeEventListener('scroll', schedule);
      window.removeEventListener('resize', schedule);
      cancelAnimationFrame(frame);
    };
  }, [exitStart]);

  return ref;
}