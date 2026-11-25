import { useEffect, useState } from 'react';
import { getOptions } from '@/api/optionApi';
import ChipSelect from '@/components/common/ChipSelect';
import { GRADES } from '@/constants/grades';
import { QUALIFICATION_TYPES } from '@/constants/qualification';

const MAX_CHIPS = 5;
const gradeLabel = (grade) => GRADES.find((g) => g.value === grade)?.label;

// 관심 분야를 고르면 그 분야의 희망 직무만, 안 고르면 전체 희망 직무
async function loadJobCategories(qualgbcd, interestFieldCodes) {
  const base = { qualgbcd: qualgbcd || undefined, limit: 100 };
  if (interestFieldCodes.length === 0) {
    return (await getOptions('job_categories', base)).items;
  }
  const pages = await Promise.all(
    interestFieldCodes.map((code) => getOptions('job_categories', { ...base, interest_field_code: code })),
  );
  return pages.flatMap((page) => page.items).sort((a, b) => a.name.localeCompare(b.name, 'ko'));
}

// 선택 목록이 바뀌면 목록에 없는 선택값은 뺀다 (그대로면 같은 배열을 돌려 다시 그리지 않음)
function keepAvailable(selected, options) {
  const kept = selected.filter((code) => options.some((o) => o.code === code));
  return kept.length === selected.length ? selected : kept;
}

export default function ConditionBar({ profile, open, onToggle, onSearch, searching, error }) {
  const [qualgbcd, setQualgbcd] = useState('');
  const [fieldOptions, setFieldOptions] = useState([]);
  const [jobOptions, setJobOptions] = useState([]);
  const [fields, setFields] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!open) return undefined;
    let ignore = false;
    getOptions('interest_fields', { qualgbcd: qualgbcd || undefined, limit: 100 })
      .then((data) => {
        if (ignore) return;
        setFieldOptions(data.items);
        setFields((prev) => keepAvailable(prev, data.items));
      })
      .catch(() => {});
    return () => {
      ignore = true;
    };
  }, [open, qualgbcd]);

  useEffect(() => {
    if (!open) return undefined;
    let ignore = false;
    loadJobCategories(qualgbcd, fields)
      .then((items) => {
        if (ignore) return;
        setJobOptions(items);
        setJobs((prev) => keepAvailable(prev, items));
      })
      .catch(() => {});
    return () => {
      ignore = true;
    };
  }, [open, qualgbcd, fields]);

  const submit = (e) => {
    e.preventDefault();
    if (fields.length === 0 && jobs.length === 0) {
      setMessage('관심 분야 또는 희망 직무를 하나 이상 골라 주세요.');
      return;
    }
    setMessage('');
    onSearch({ qualgbcd: qualgbcd || null, interest_field_codes: fields, job_category_codes: jobs });
  };

  const school = profile?.university_id
    ? [profile.university_name, profile.major_name ?? '학부 전체', gradeLabel(profile.grade)]
        .filter(Boolean)
        .join(' · ')
    : '학교 정보 없음';

  return (
    <div className="condition-bar">
      <div className="condition-bar__summary">
        <span className="condition-bar__label">현재 선택 조건</span>
        <span>{school}</span>
        <button type="button" className="condition-bar__toggle" aria-expanded={open} onClick={onToggle}>
          {open ? '조건 검색 접기' : '관심 분야·희망 직무로 찾기'}
        </button>
      </div>

      {open && (
        <form className="condition-bar__panel" onSubmit={submit}>
          <fieldset className="condition-bar__group">
            <legend>자격 구분</legend>
            {[{ value: '', label: '전체' }, ...QUALIFICATION_TYPES].map((type) => (
              <label key={type.value} className="condition-bar__radio">
                <input
                  type="radio"
                  name="qualgbcd"
                  value={type.value}
                  checked={qualgbcd === type.value}
                  onChange={() => setQualgbcd(type.value)}
                />
                {type.label}
              </label>
            ))}
          </fieldset>
          <fieldset className="condition-bar__group">
            <legend>
              관심 분야 <small>(최대 {MAX_CHIPS}개)</small>
            </legend>
            <ChipSelect options={fieldOptions} value={fields} onChange={setFields} max={MAX_CHIPS} />
          </fieldset>
          <fieldset className="condition-bar__group">
            <legend>
              희망 직무 <small>(최대 {MAX_CHIPS}개)</small>
            </legend>
            <ChipSelect options={jobOptions} value={jobs} onChange={setJobs} max={MAX_CHIPS} />
          </fieldset>
          {(message || error) && <p className="condition-bar__error">{message || error}</p>}
          <button type="submit" className="btn btn--primary" disabled={searching}>
            {searching ? '찾는 중' : '조건으로 추천받기'}
          </button>
        </form>
      )}
    </div>
  );
}
