import { REPORT_LABELS } from '@/components/passstory/labels';
import '@/styles/passstory.css';

export default function HiddenNotice({ status, hiddenReason }) {
  if (status === 'HIDDEN') {
    const reason = REPORT_LABELS[hiddenReason];
    return (
      <p className="hidden-notice" role="note">
        신고로 숨김 처리된 글입니다.{reason && ` (사유: ${reason})`}
      </p>
    );
  }
  if (status === 'REVIEW_PENDING') {
    return (
      <p className="hidden-notice" role="note">
        수정 후 관리자 재검토를 기다리는 글입니다. 승인 전까지 다른 회원에게 보이지 않습니다.
      </p>
    );
  }
  return null;
}
