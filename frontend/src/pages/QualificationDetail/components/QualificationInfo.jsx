const NO_INFO = '정보 없음';

// '필기 : … - 실기 : …'처럼 단계가 이어진 원문을 단계별 줄로 나눔
const stageLines = (text) => text.split(/\s-\s(?=\S+\s*:)/);

// 자격증 머리글(종목명·자격구분·영문명·관련부처·시행기관)과 취득방법
export default function QualificationInfo({ qualification: q }) {
  const steps = [
    ['시 행 처', q.conducting_agency],
    ['관련학과', q.related_majors],
    ['시험과목', q.exam_subjects],
    ['검정방법', q.exam_method],
    ['합격기준', q.exam_standard],
  ];

  return (
    <>
      <section className="qualification-card">
        <div className="qualification-head">
          <div>
            <h2 className="qualification-head__name">
              {q.jmfldnm}
              <span className="qualification-badge">{q.qualgbnm}</span>
            </h2>
            {q.eng_name && <p className="qualification-head__eng">{q.eng_name}</p>}
          </div>
          <dl className="qualification-head__meta">
            <dt>관련부처</dt>
            <dd>{q.related_ministry || NO_INFO}</dd>
            <dt>시행기관</dt>
            <dd>{q.conducting_agency || NO_INFO}</dd>
          </dl>
        </div>
      </section>

      <section className="qualification-card">
        <h2 className="qualification-card__title">취득방법</h2>
        <ol className="qualification-steps">
          {steps.map(([label, text], i) => (
            <li key={label}>
              <strong>
                {String.fromCharCode(0x2460 + i)} {label}
              </strong>
              {text ? stageLines(text).map((line) => <p key={line}>- {line}</p>) : <p>{NO_INFO}</p>}
            </li>
          ))}
        </ol>
      </section>
    </>
  );
}
