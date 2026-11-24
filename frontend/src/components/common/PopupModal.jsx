import '@/styles/popup.css';

function CheckIcon() {
  return (
    <svg className="popup__icon" viewBox="0 0 100 100" aria-hidden="true">
      <circle cx="50" cy="50" r="50" fill="#e8bcce" />
      <path
        d="M30 51 L44 65 L71 37"
        fill="none"
        stroke="#ffffff"
        strokeWidth="7"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

/**
 * 공통 팝업 (650 x 400)
 * buttons: [{ text, onClick, variant: 'primary' | 'secondary' }] — 1개 또는 2개
 */
export default function PopupModal({ title, message, buttons = [] }) {
  const single = buttons.length === 1;

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="popup-title">
      <div className="popup">
        <CheckIcon />
        <h2 id="popup-title" className="popup__title">
          {title}
        </h2>
        {message && <p className="popup__message">{message}</p>}
        <div className={`popup__actions${single ? ' popup__actions--single' : ''}`}>
          {buttons.map((button) => (
            <button
              key={button.text}
              type="button"
              className={`popup__button popup__button--${button.variant ?? 'primary'}`}
              onClick={button.onClick}
            >
              {button.text}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
