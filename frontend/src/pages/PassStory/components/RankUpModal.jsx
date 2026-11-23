import AlertModal from '@/components/common/AlertModal';
import { RANK_LABELS } from '@/components/passstory/labels';

// rankUp = 등록 응답 {ranked_up, previous_rank, new_rank}
export default function RankUpModal({ rankUp, onClose }) {
  return (
    <AlertModal
      message={`축하합니다! 저격 등급이 ${RANK_LABELS[rankUp.previous_rank]}에서 ${RANK_LABELS[rankUp.new_rank]}(으)로 올랐습니다.`}
      onClose={onClose}
    />
  );
}
