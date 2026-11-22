import ConfirmModal from '@/components/common/ConfirmModal';

export default function LoginRequiredModal({ onConfirm, onCancel }) {
  return (
    <ConfirmModal
      message="로그인이 필요한 기능입니다."
      confirmText="로그인하러 가기"
      cancelText="취소하기"
      onConfirm={onConfirm}
      onCancel={onCancel}
    />
  );
}
