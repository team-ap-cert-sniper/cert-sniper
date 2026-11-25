import { useState } from 'react';
import RecommendCard from '@/pages/Recommend/components/RecommendCard';

// 가운데 카드 기준 상대 위치 (-1 왼쪽, 0 가운데, 1 오른쪽). 끝과 처음이 이어짐
function offsetOf(i, index, count) {
  const d = (i - index + count) % count;
  return d > count / 2 ? d - count : d;
}

// 추천 카드 최대 5개: 커버플로우(가운데 카드 크게, 양옆 카드 작게 겹침). 1개면 화살표 없음
export default function RecommendCarousel({ items, scoreBasisDate, onToggleFavorite }) {
  const [index, setIndex] = useState(0);
  const multiple = items.length > 1;
  const move = (step) => setIndex((current) => (current + step + items.length) % items.length);

  return (
    <section className="recommend-box recommend-carousel" aria-roledescription="carousel" aria-label="추천 자격증">
      {multiple && (
        <button type="button" className="recommend-carousel__arrow" aria-label="이전 추천" onClick={() => move(-1)}>
          ‹
        </button>
      )}
      <div className="coverflow">
        {items.map((item, i) => {
          const offset = offsetOf(i, index, items.length);
          const center = offset === 0;
          return (
            <div
              key={item.jmcd}
              className={`coverflow__slide${center ? ' is-center' : ''}${Math.abs(offset) > 1 ? ' is-hidden' : ''}`}
              style={{ '--offset': offset }}
              onClick={center ? undefined : () => setIndex(i)}
              aria-hidden={!center}
            >
              <RecommendCard
                item={item}
                rank={i + 1}
                scoreBasisDate={scoreBasisDate}
                onToggleFavorite={onToggleFavorite}
                inert={!center}
              />
            </div>
          );
        })}
      </div>
      {multiple && (
        <button type="button" className="recommend-carousel__arrow" aria-label="다음 추천" onClick={() => move(1)}>
          ›
        </button>
      )}
      {multiple && (
        <div className="recommend-carousel__dots">
          {items.map((item, i) => (
            <button
              key={item.jmcd}
              type="button"
              className={i === index ? 'recommend-carousel__dot is-active' : 'recommend-carousel__dot'}
              aria-label={`${i + 1}번째 추천`}
              aria-current={i === index}
              onClick={() => setIndex(i)}
            />
          ))}
        </div>
      )}
    </section>
  );
}
