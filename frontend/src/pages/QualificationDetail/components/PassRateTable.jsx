import { stageOrder } from '@/constants/qualification';
import { formatNumber, formatPercent } from '@/utils/format';

const RECENT_YEARS = 10;

// 종목별 검정현황: 최근 10년 단계별 응시·합격·합격률
export default function PassRateTable({ passRates, years }) {
  const recent = years.slice(0, RECENT_YEARS);
  const stages = [...new Set(passRates.filter((r) => recent.includes(r.year)).map((r) => r.exam_stage))].sort(
    (a, b) => stageOrder(a) - stageOrder(b),
  );
  const find = (year, stage) => passRates.find((r) => r.year === year && r.exam_stage === stage);

  return (
    <section className="qualification-card">
      <h2 className="qualification-card__title">종목별 검정현황</h2>
      <div className="qualification-table-wrap">
        <table className="qualification-table">
          <thead>
            <tr>
              <th rowSpan={2}>연도</th>
              {stages.map((stage) => (
                <th key={stage} colSpan={3}>
                  {stage}
                </th>
              ))}
            </tr>
            <tr>
              {stages.flatMap((stage) => [
                <th key={`${stage}-n`}>응시</th>,
                <th key={`${stage}-k`}>합격</th>,
                <th key={`${stage}-p`}>합격률</th>,
              ])}
            </tr>
          </thead>
          <tbody>
            {recent.map((year) => (
              <tr key={year}>
                <th scope="row">{year}</th>
                {stages.flatMap((stage) => {
                  const r = find(year, stage);
                  return [
                    <td key={`${stage}-n`}>{formatNumber(r?.examinees)}</td>,
                    <td key={`${stage}-k`}>{formatNumber(r?.passers)}</td>,
                    <td key={`${stage}-p`}>{formatPercent(r?.pass_rate_pct)}</td>,
                  ];
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
