import { stageOrder } from '@/constants/qualification';
import { formatDate } from '@/utils/date';

const NOTES = [
  '원서접수시간은 원서접수 첫날 10:00부터 마지막 날 18:00까지임',
  '필기시험 합격예정자 및 최종합격자 발표시간은 해당 발표일 09:00임',
  '시험 일정은 종목별, 지역별로 상이할 수 있음',
  '접수 일정 전에 공지되는 해당 회별 수험자 안내(Q-net 공지사항 게시) 참조 필수',
];

const period = (start, end) => {
  if (!start) return '-';
  return start === end || !end ? formatDate(start) : `${formatDate(start)} ~ ${formatDate(end)}`;
};

// 같은 회차의 단계(필기·실기, 1차·2차…)를 묶고, 회차는 시험일 순
function groupByRound(schedules) {
  const sorted = [...schedules].sort(
    (a, b) => a.exam_start_date.localeCompare(b.exam_start_date) || stageOrder(a.exam_stage) - stageOrder(b.exam_stage),
  );
  const rounds = new Map();
  sorted.forEach((s) => rounds.set(s.exam_round, [...(rounds.get(s.exam_round) ?? []), s]));
  return [...rounds.entries()];
}

export default function ExamScheduleTable({ name, schedules }) {
  return (
    <section className="qualification-card">
      <h2 className="qualification-card__title">시험일정</h2>
      <p className="qualification-card__sub">
        {name}(원서접수시간은 원서접수 첫날 10:00부터 <mark>마지막 날 18:00까지</mark> 임)
      </p>
      {schedules.length === 0 ? (
        <p className="qualification-empty">시험일정 정보가 없습니다.</p>
      ) : (
        <div className="qualification-table-wrap">
          <table className="qualification-table">
            <thead>
              <tr>
                <th>구분</th>
                <th>단계</th>
                <th>
                  원서접수
                  <br />
                  <small>(빈자리접수)</small>
                </th>
                <th>시험일</th>
                <th>합격(예정자)발표</th>
                <th>상태</th>
              </tr>
            </thead>
            <tbody>
              {groupByRound(schedules).map(([round, stages]) =>
                stages.map((s, i) => (
                  <tr key={s.schedule_id}>
                    {i === 0 && (
                      <th scope="rowgroup" rowSpan={stages.length}>
                        {round}
                      </th>
                    )}
                    <td>{s.exam_stage}</td>
                    <td>
                      {period(s.registration_start_date, s.registration_end_date)}
                      {s.vacancy_registration_start_date && (
                        <small>
                          [빈자리접수 : {period(s.vacancy_registration_start_date, s.vacancy_registration_end_date)}]
                        </small>
                      )}
                    </td>
                    <td>{period(s.exam_start_date, s.exam_end_date)}</td>
                    <td>{period(s.pass_announcement_date)}</td>
                    <td className={s.exam_status === 'ENDED' ? 'is-ended' : undefined}>{s.dday_label}</td>
                  </tr>
                )),
              )}
            </tbody>
          </table>
        </div>
      )}
      <ul className="qualification-notes">
        {NOTES.map((note) => (
          <li key={note}>{note}</li>
        ))}
      </ul>
    </section>
  );
}
