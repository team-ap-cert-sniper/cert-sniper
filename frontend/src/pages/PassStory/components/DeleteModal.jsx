// 합격수기 삭제 확인 (디자인 전용 팝업, 동작은 공통 ConfirmModal과 같음)
export default function DeleteModal({ onConfirm, onCancel }) {
  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="delete-title">
      <div className="modal ps-modal ps-modal--center">
        <span className="ps-modal__icon" aria-hidden="true">
          🗑
        </span>
        <h2 id="delete-title">작성하신 합격 후기를 삭제하시겠습니까?</h2>
        <p>삭제한 후기는 복구할 수 없으며, 저격 등급이 다시 계산됩니다.</p>
        <div className="modal__actions">
          <button type="button" className="btn" onClick={onCancel}>
            유지하기
          </button>
          <button type="button" className="btn btn--primary" onClick={onConfirm}>
            삭제하기
          </button>
        </div>
      </div>
    </div>
  );
}
