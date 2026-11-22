export default function ConfirmModal({ message, confirmText = '확인', cancelText = '취소', onConfirm, onCancel }) {
  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true">
      <div className="modal">
        <p>{message}</p>
        <div className="modal__actions">
          <button type="button" className="btn" onClick={onCancel}>{cancelText}</button>
          <button type="button" className="btn btn--primary" onClick={onConfirm}>{confirmText}</button>
        </div>
      </div>
    </div>
  );
}
