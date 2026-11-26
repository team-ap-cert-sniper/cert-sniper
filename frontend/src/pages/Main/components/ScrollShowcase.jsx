import { useEffect, useRef } from 'react';
import '@/styles/main-showcase.css';

const STEPS = [
  {
    word: 'Recommend',
    title: '맞춤 자격증 추천',
    desc: '내 전공과 학년에 딱 맞는 자격증',
    card: '전공 맞춤 자격증 추천 애니메이션',
    align: 'right',
  },
  {
    word: 'Planner',
    title: '플래너 다이어리',
    desc: '자유롭게 세우는 나만의 학습 계획',
    card: 'AI에게 플래너를 부탁하고 다이어리 페이지로 넘어가는 애니메이션',
    align: 'left',
  },
  {
    word: 'Story',
    title: '합격수기',
    desc: '먼저 합격한 사람들의 이야기',
    card: '합격수기 애니메이션',
    align: 'right',
  },
];

const MIN_OPACITY = 0.12;
// 마지막 단어가 사라지는 속도: 마지막 장면을 0.4화면(1 / 2.5) 지나면 완전히 사라짐
const LAST_FADE_SPEED = 2.5;

export default function ScrollShowcase() {
  const rootRef = useRef(null);
  const wordRefs = useRef([]);

  useEffect(() => {
    let frame = 0;
    const lastIndex = STEPS.length - 1;

    const update = () => {
      frame = 0;
      const root = rootRef.current;
      if (!root) return;
      const progress = -root.getBoundingClientRect().top / window.innerHeight;
      const current = Math.min(Math.max(Math.round(progress), 0), lastIndex);
      wordRefs.current.forEach((el, i) => {
        if (!el) return;
        const distance = Math.abs(progress - i);
        let opacity = Math.max(MIN_OPACITY, 1 - distance * 1.8);
        // 마지막 단어(Story)는 마지막 장면을 지나가면 0까지 사라져서 아래 인기 자격증을 가리지 않게
        const passedLast = progress - lastIndex;
        if (i === lastIndex && passedLast > 0) {
          opacity = Math.max(0, 1 - passedLast * LAST_FADE_SPEED);
        }
        el.style.opacity = i === current ? String(opacity) : '0';
      });
    };

    const schedule = () => {
      if (!frame) frame = requestAnimationFrame(update);
    };

    update();
    window.addEventListener('scroll', schedule, { passive: true });
    window.addEventListener('resize', schedule);
    return () => {
      window.removeEventListener('scroll', schedule);
      window.removeEventListener('resize', schedule);
      cancelAnimationFrame(frame);
    };
  }, []);

  return (
    <section className="showcase" ref={rootRef}>
      <div className="showcase__stage" aria-hidden="true">
        {STEPS.map((step, i) => (
          <span
            key={step.word}
            className="showcase__word"
            ref={(el) => {
              wordRefs.current[i] = el;
            }}
          >
            {step.word}
          </span>
        ))}
      </div>

      {STEPS.map((step) => (
        <div key={step.word} className={`showcase__step showcase__step--${step.align}`}>
          <div className="showcase__item">
            <h2 className="showcase__title">{step.title}</h2>
            <p className="showcase__desc">{step.desc}</p>
            <div className="showcase__card">{step.card}</div>
          </div>
        </div>
      ))}
    </section>
  );
}