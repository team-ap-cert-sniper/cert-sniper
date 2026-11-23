import { useState } from 'react';
import { REPORT_TYPES } from '@/constants/report';
import { REPORT_LABELS } from '@/components/passstory/labels';

export default function ReportModal({ onSubmit, onCancel }) {
  const [reportType, setReportType] = useState('');

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="report-title">
      <div className="modal">
        <h2 id="report-title">신고하기</h2>
        <p>신고하면 글이 즉시 숨김 처리되고 관리자가 검토합니다.</p>
        <fieldset>
          <legend>신고 사유</legend>
          {REPORT_TYPES.map((type) => (
            <label key={type}>
              <input
                type="radio"
                name="reportType"
                value={type}
                checked={reportType === type}
                onChange={() => setReportType(type)}
              />
              {REPORT_LABELS[type]}
            </label>
          ))}
        </fieldset>
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
            신고
          </button>
        </div>
      </div>
    </div>
  );
}
