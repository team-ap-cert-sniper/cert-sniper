import { formatNumber } from '@/utils/format';

const fee = (value) => (value === null || value === undefined ? '-' : `${formatNumber(value)}원`);

// 시험정보: 수수료와 응시자격(개인 응시자격 확정이 아니라 Q-Net 안내 기준)
export default function EligibilityInfo({ qualification: q, eligibility }) {
  return (
    <section className="qualification-card">
      <h2 className="qualification-card__title">시험정보</h2>
      <h3 className="qualification-card__label">수수료</h3>
      <div className="qualification-table-wrap">
        <table className="qualification-table">
          <thead>
            <tr>
              <th>필기</th>
              <th>실기</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>{fee(q.fee_written)}</td>
              <td>{fee(q.fee_practical)}</td>
            </tr>
          </tbody>
        </table>
      </div>
      {q.fee_note && <p className="qualification-card__sub">{q.fee_note}</p>}

      <h3 className="qualification-card__label">응시자격</h3>
      <p>{eligibility.summary || (eligibility.required === false ? '응시자격 제한 없음' : '응시자격 정보 없음')}</p>
      {eligibility.conditions.length > 0 && (
        <ul className="qualification-notes">
          {eligibility.conditions.map((c) => (
            <li key={`${c.type}-${c.text}`}>{c.text}</li>
          ))}
        </ul>
      )}
      <a className="qualification-link" href={eligibility.source_url} target="_blank" rel="noreferrer">
        Q-Net 응시자격 자가진단 확인
      </a>
    </section>
  );
}
