import { useState } from 'react';
import { REPORT_TYPES } from '@/constants/report';
import { REPORT_DESCRIPTIONS, REPORT_LABELS } from '@/components/passstory/labels';

export default function ReportModal({ onSubmit, onCancel }) {
  const [reportType, setReportType] = useState('');

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="report-title">
      <div className="modal ps-modal">
        <div className="ps-modal__head">
          <h2 id="report-title">게시글 신고하기</h2>
          <span className="ps-chip ps-chip--danger">신고 접수</span>
          <button type="button" className="ps-modal__close" aria-label="닫기" onClick={onCancel}>
            ×
          </button>
        </div>
        <div className="ps-modal__body">
          <fieldset>
            <legend className="ps-required">신고 사유 선택</legend>
            {REPORT_TYPES.map((type) => (
              <label key={type} className="ps-radio">
                <input
                  type="radio"
                  name="reportType"
                  value={type}
                  checked={reportType === type}
                  onChange={() => setReportType(type)}
                />
                <span>
                  {REPORT_LABELS[type]}
                  <small>{REPORT_DESCRIPTIONS[type]}</small>
                </span>
              </label>
            ))}
          </fieldset>
          <p className="ps-modal__info">
            신고하면 글이 즉시 숨김 처리되고, 관리자 검토 후 내부 규정에 따라 처리됩니다.
          </p>
          <div className="modal__actions">
            <button type="button" className="btn" onClick={onCancel}>
              취소
            </button>
            <button
              type="button"
              className="btn btn--primary"
              disabled={!reportType}
              onClick={() => onSubmit(reportType)}
            >
              신고하기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
