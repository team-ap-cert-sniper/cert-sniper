import { useEffect, useState } from 'react';
import { getOptions } from '@/api/optionApi';
import ChipSelect from '@/components/common/ChipSelect';
import { GRADES } from '@/constants/grades';
import { QUALIFICATION_TYPES } from '@/constants/qualification';

const MAX_CHIPS = 5;
// 학년 표시: 숫자 학년은 'n학년 재학 중', 그 외(졸업·비재학)는 이름 그대로
function gradeText(grade) {
  const label = GRADES.find((g) => g.value === grade)?.label;
  return label && /^\d+$/.test(grade) ? `${label} 재학 중` : label;
}

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

const namesOf = (codes, options) => codes.map((code) => options.find((o) => o.code === code)?.name).filter(Boolean);

// 검색어로 칩 목록을 좁히되, 이미 고른 칩은 항상 보이게
const filterChips = (options, keyword, selected) =>
  keyword ? options.filter((o) => o.name.includes(keyword) || selected.includes(o.code)) : options;

export default function ConditionBar({ profile, applied, open, onToggle, onSearch, onReset, searching, error }) {
  const [qualgbcd, setQualgbcd] = useState('');
  const [fieldOptions, setFieldOptions] = useState([]);
  const [jobOptions, setJobOptions] = useState([]);
  const [fields, setFields] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [fieldKeyword, setFieldKeyword] = useState('');
  const [jobKeyword, setJobKeyword] = useState('');
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
    onSearch({
      qualgbcd: qualgbcd || null,
      interest_field_codes: fields,
      job_category_codes: jobs,
      labels: { fields: namesOf(fields, fieldOptions), jobs: namesOf(jobs, jobOptions) },
    });
  };

  const school = profile?.university_id
    ? [profile.university_name, profile.major_name ?? '학부 전체', gradeText(profile.grade)].filter(Boolean).join(' ')
    : '학교 정보 없음';

  return (
    <section className="recommend-box condition-bar">
      <div className="condition-bar__summary">
        <div className="condition-bar__current">
          <span className="recommend-pill recommend-pill--pink">현재 선택 조건</span>
          <strong>{school}</strong>
          <span className="condition-bar__divider" aria-hidden="true">
            |
          </span>
          <span>
            관심 분야: <strong>{applied?.fields.join(', ') || '-'}</strong>
          </span>
          <span>
            희망 직무: <strong>{applied?.jobs.join(', ') || '-'}</strong>
          </span>
        </div>
        <button
          type="button"
          className={open ? 'condition-bar__toggle is-open' : 'condition-bar__toggle'}
          aria-expanded={open}
          aria-label={open ? '조건 검색 접기' : '관심 분야·희망 직무로 찾기'}
          onClick={onToggle}
        >
          ›
        </button>
      </div>

      {open && (
        <form className="condition-bar__form" onSubmit={submit}>
          <select
            className="condition-bar__type"
            aria-label="자격 구분"
            value={qualgbcd}
            onChange={(e) => setQualgbcd(e.target.value)}
          >
            <option value="">전체</option>
            {QUALIFICATION_TYPES.map((type) => (
              <option key={type.value} value={type.value}>
                {type.label.replace('국가', '')}
              </option>
            ))}
          </select>
          <input
            className="condition-bar__search"
            type="search"
            aria-label="관심 분야 검색하기"
            value={fieldKeyword}
            onChange={(e) => setFieldKeyword(e.target.value.trim())}
            placeholder="관심 분야 검색하기"
          />
          <input
            className="condition-bar__search"
            type="search"
            aria-label="희망 직무 검색하기"
            value={jobKeyword}
            onChange={(e) => setJobKeyword(e.target.value.trim())}
            placeholder="희망 직무 검색하기"
          />
          <button type="submit" className="condition-bar__submit" disabled={searching}>
            {searching ? '찾는 중' : '검색'}
          </button>
          <div className="condition-bar__chips condition-bar__chips--fields">
            <p>
              관심 분야 <small>(최대 {MAX_CHIPS}개)</small>
            </p>
            <ChipSelect
              options={filterChips(fieldOptions, fieldKeyword, fields)}
              value={fields}
              onChange={setFields}
              max={MAX_CHIPS}
            />
          </div>
          <div className="condition-bar__chips condition-bar__chips--jobs">
            <p>
              희망 직무 <small>(최대 {MAX_CHIPS}개)</small>
            </p>
            <ChipSelect
              options={filterChips(jobOptions, jobKeyword, jobs)}
              value={jobs}
              onChange={setJobs}
              max={MAX_CHIPS}
            />
          </div>
          <div className="condition-bar__footer">
            {(message || error) && <p className="condition-bar__error">{message || error}</p>}
            {onReset && (
              <button type="button" className="condition-bar__reset" onClick={onReset}>
                내 원래 자격증 추천 보기
              </button>
            )}
          </div>
        </form>
      )}
    </section>
  );
}
