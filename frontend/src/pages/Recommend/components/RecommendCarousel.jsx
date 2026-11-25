import { useState } from 'react';
import RecommendCard from '@/pages/Recommend/components/RecommendCard';

// 추천 카드 최대 5개를 한 장씩 넘겨 본다. 1개면 화살표·점 표시 없음
export default function RecommendCarousel({ items, scoreBasisDate, onToggleFavorite }) {
  const [index, setIndex] = useState(0);
  const multiple = items.length > 1;
  const move = (step) => setIndex((current) => (current + step + items.length) % items.length);

  return (
    <div className={multiple ? 'recommend-carousel' : 'recommend-carousel is-single'}>
      {multiple && (
        <button type="button" className="recommend-carousel__arrow" aria-label="이전 추천" onClick={() => move(-1)}>
          ‹
        </button>
      )}
      <RecommendCard
        item={items[index]}
        rank={index + 1}
        scoreBasisDate={scoreBasisDate}
        onToggleFavorite={onToggleFavorite}
      />
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
    </div>
  );
}
