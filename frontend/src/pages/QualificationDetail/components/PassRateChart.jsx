import { useState } from 'react';
import { BarElement, CategoryScale, Chart as ChartJS, Legend, LinearScale, Tooltip } from 'chart.js';
import { Bar } from 'react-chartjs-2';
import { stageOrder } from '@/constants/qualification';
import { formatNumber, formatPercent } from '@/utils/format';

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const SERIES = [
  { key: 'applicants', label: '접수자', color: '#454b5a' },
  { key: 'examinees', label: '응시자', color: '#e8bcce' },
  { key: 'passers', label: '합격자', color: '#dfa9be' },
];

// 연도별 단계 통계: 연도를 골라 접수자·응시자·합격자 막대그래프와 표
export default function PassRateChart({ passRates, years }) {
  const [pending, setPending] = useState(years[0]);
  const [year, setYear] = useState(years[0]);
  const rows = passRates
    .filter((r) => r.year === year)
    .sort((a, b) => stageOrder(a.exam_stage) - stageOrder(b.exam_stage));

  const data = {
    labels: rows.map((r) => r.exam_stage),
    datasets: SERIES.map((s) => ({ label: s.label, data: rows.map((r) => r[s.key] ?? 0), backgroundColor: s.color })),
  };
  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom' },
      tooltip: { callbacks: { label: (ctx) => `${ctx.dataset.label} ${formatNumber(ctx.raw)}명` } },
    },
    scales: { y: { beginAtZero: true } },
  };

  return (
    <section className="qualification-card">
      <form
        className="qualification-year"
        onSubmit={(e) => {
          e.preventDefault();
          setYear(pending);
        }}
      >
        <label htmlFor="pass-rate-year">년도</label>
        <select id="pass-rate-year" value={pending} onChange={(e) => setPending(Number(e.target.value))}>
          {years.map((y) => (
            <option key={y} value={y}>
              {y}
            </option>
          ))}
        </select>
        <button type="submit" className="qualification-button">
          검색
        </button>
      </form>
      <div className="qualification-chart">
        <Bar data={data} options={options} />
      </div>
      <div className="qualification-table-wrap">
        <table className="qualification-table">
          <thead>
            <tr>
              <th>분류</th>
              <th>접수자</th>
              <th>응시자</th>
              <th>응시율(%)</th>
              <th>합격자</th>
              <th>합격률(%)</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.exam_stage}>
                <th scope="row">{r.exam_stage}</th>
                <td>{formatNumber(r.applicants)}</td>
                <td>{formatNumber(r.examinees)}</td>
                <td>{formatPercent(r.attendance_rate_percent)}</td>
                <td>{formatNumber(r.passers)}</td>
                <td>{formatPercent(r.pass_rate_pct)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
