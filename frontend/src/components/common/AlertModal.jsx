export default function AlertModal({ message, buttonText = '확인', onClose }) {
  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true">
      <div className="modal">
        <p>{message}</p>
        <div className="modal__actions">
          <button type="button" className="btn btn--primary" onClick={onClose}>{buttonText}</button>
        </div>
      </div>
    </div>
  );
}
