import { useState } from 'react';
import { QUALIFICATION_TYPES } from '@/constants/qualification';
import useOptions from '@/hooks/useOptions';

const LIMIT = 100;

function Column({ items, emptyMessage, selected, onSelect, wide = false, labelOf = (item) => item.name, keyOf }) {
  if (items === null) return <div className="qualification-browser__column is-empty">불러오는 중입니다.</div>;
  if (items.length === 0) return <div className="qualification-browser__column is-empty">ⓘ {emptyMessage}</div>;
  return (
    <ul className={wide ? 'qualification-browser__column is-wide' : 'qualification-browser__column'}>
      {items.map((item) => (
        <li key={keyOf(item)}>
          <button
            type="button"
            className={selected === keyOf(item) ? 'is-active' : undefined}
            onClick={() => onSelect(keyOf(item))}
          >
            {labelOf(item)}
          </button>
        </li>
      ))}
    </ul>
  );
}

// 자격증 탐색: 자격구분 → 직무분야 → 분류 → 종목
export default function QualificationBrowser({ selectedJmcd, onSelect }) {
  const [qualgbcd, setQualgbcd] = useState('T');
  const [field, setField] = useState(null);
  const [job, setJob] = useState(null);

  const fields = useOptions('interest_fields', { qualgbcd, limit: LIMIT });
  const jobs = useOptions('job_categories', field ? { qualgbcd, interest_field_code: field, limit: LIMIT } : null);
  const qualifications = useOptions(
    'qualifications',
    job ? { qualgbcd, interest_field_code: field, job_category_code: job, limit: LIMIT } : null,
  );

  const chooseType = (value) => {
    setQualgbcd(value);
    setField(null);
    setJob(null);
  };
  const chooseField = (code) => {
    setField(code);
    setJob(null);
  };

  return (
    <div className="qualification-browser">
      <ul className="qualification-browser__tabs">
        {QUALIFICATION_TYPES.map((type) => (
          <li key={type.value}>
            <button
              type="button"
              className={qualgbcd === type.value ? 'is-active' : undefined}
              onClick={() => chooseType(type.value)}
            >
              {type.label}
            </button>
          </li>
        ))}
      </ul>
      <Column items={fields} selected={field} onSelect={chooseField} wide keyOf={(item) => item.code} />
      <Column
        items={field ? jobs : []}
        emptyMessage="직무분야를 선택해주세요."
        selected={job}
        onSelect={setJob}
        keyOf={(item) => item.code}
      />
      <Column
        items={job ? qualifications : []}
        emptyMessage="분류를 선택해주세요."
        selected={selectedJmcd}
        onSelect={onSelect}
        keyOf={(item) => item.jmcd}
        labelOf={(item) => item.jmfldnm}
      />
    </div>
  );
}
