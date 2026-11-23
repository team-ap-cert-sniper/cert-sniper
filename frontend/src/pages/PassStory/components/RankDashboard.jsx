import { useEffect, useState } from 'react';
import { getMyRank } from '@/api/rankApi';
import RankBadge from '@/components/passstory/RankBadge';
import { RANK_LABELS } from '@/components/passstory/labels';
import { errorMessage } from '@/components/passstory/utils';

export default function RankDashboard() {
  const [rank, setRank] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getMyRank()
      .then(setRank)
      .catch((e) => setError(errorMessage(e)));
  }, []);

  if (error) return <p className="field-error">{error}</p>;
  if (!rank) return null;

  return (
    <section className="rank-dashboard">
      <h2>내 저격 등급</h2>
      <p>
        현재 등급 <RankBadge rank={rank.current_rank} /> · 합격 자격증 {rank.valid_cert_count}개
      </p>
      <p>
        {rank.next_rank
          ? `다음 등급 ${RANK_LABELS[rank.next_rank]}까지 자격증 ${rank.remaining_for_next}개`
          : '최고 등급입니다.'}
      </p>
      <p>기준: 훈련병 0개 · 견습 스나이퍼 1개 · 명사수 3개 · 전설의 스나이퍼 5개 (서로 다른 자격증 수)</p>
    </section>
  );
}
