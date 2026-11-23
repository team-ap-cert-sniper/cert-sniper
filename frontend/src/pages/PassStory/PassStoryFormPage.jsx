import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getOptions } from '@/api/optionApi';
import { createPassStory, getPassStory, updatePassStory } from '@/api/passStoryApi';
import ConfirmModal from '@/components/common/ConfirmModal';
import FieldError from '@/components/common/FieldError';
import Loading from '@/components/common/Loading';
import SearchSelect from '@/components/common/SearchSelect';
import HiddenNotice from '@/components/passstory/HiddenNotice';
import { ATTEMPT_LABELS, EXAMINEE_BASE_LABELS, PREP_PERIOD_LABELS } from '@/components/passstory/labels';
import { errorMessage, htmlToText, textToHtml } from '@/components/passstory/utils';

const MAX_BODY = 1500;
const EMPTY_FORM = {
  jmcd: '',
  jmfldnm: '',
  text: '',
  examinee_base: '',
  prep_period: '',
  attempt_band: '',
  textbook_name: '',
  pass_score: '',
  exam_date: '',
};

// SearchSelect가 fetchOptions 변경마다 다시 조회하므로 모듈 상수로 고정
const fetchQualifications = (query) => getOptions('qualifications', { query, limit: 20 });

function CodeSelect({ id, label, value, labels, onChange }) {
  return (
    <p>
      <label htmlFor={id}>{label}</label>
      <select id={id} value={value} onChange={(e) => onChange(e.target.value)}>
        <option value="">선택 안 함</option>
        {Object.entries(labels).map(([code, text]) => (
          <option key={code} value={code}>
            {text}
          </option>
        ))}
      </select>
    </p>
  );
}

export default function PassStoryFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [form, setForm] = useState(EMPTY_FORM);
  const [status, setStatus] = useState('PUBLISHED');
  const [loaded, setLoaded] = useState(!isEdit);
  const [loadError, setLoadError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitError, setSubmitError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [confirmCancel, setConfirmCancel] = useState(false);

  useEffect(() => {
    if (!isEdit) return;
    getPassStory(id)
      .then((s) => {
        if (!s.mine) {
          setLoadError('본인 글만 수정할 수 있습니다.');
          return;
        }
        setForm({
          jmcd: s.jmcd,
          jmfldnm: s.jmfldnm,
          text: htmlToText(s.body),
          examinee_base: s.examinee_base ?? '',
          prep_period: s.prep_period ?? '',
          attempt_band: s.attempt_band ?? '',
          textbook_name: s.textbook_name ?? '',
          pass_score: s.pass_score ?? '',
          exam_date: s.exam_date ?? '',
        });
        setStatus(s.status);
        setLoaded(true);
      })
      .catch((e) => setLoadError(errorMessage(e)));
  }, [id, isEdit]);

  const set = (key) => (value) => setForm((f) => ({ ...f, [key]: value }));
  const bodyHtml = textToHtml(form.text);

  const submit = async (e) => {
    e.preventDefault();
    const errors = {};
    if (!form.jmcd) errors.jmcd = '자격증을 선택해 주세요.';
    if (!form.text.trim()) errors.text = '상세 후기를 입력해 주세요.';
    if (bodyHtml.length > MAX_BODY) errors.text = `상세 후기는 ${MAX_BODY}자 이내로 입력해 주세요.`;
    setFieldErrors(errors);
    if (Object.keys(errors).length) return;

    const request = {
      jmcd: form.jmcd,
      body: bodyHtml,
      examinee_base: form.examinee_base || null,
      prep_period: form.prep_period || null,
      attempt_band: form.attempt_band || null,
      textbook_name: form.textbook_name.trim() || null,
      pass_score: form.pass_score === '' ? null : Number(form.pass_score),
      exam_date: form.exam_date || null,
    };

    setSubmitting(true);
    setSubmitError('');
    try {
      if (isEdit) {
        await updatePassStory(id, request);
        navigate(`/pass-stories/${id}`);
      } else {
        const result = await createPassStory(request);
        navigate('/pass-stories', { state: result.ranked_up ? { rankUp: result } : null });
      }
    } catch (err) {
      setSubmitError(errorMessage(err, { NOT_FOUND: '선택한 자격증을 찾을 수 없습니다.' }));
      setSubmitting(false);
    }
  };

  if (loadError) return <p className="field-error">{loadError}</p>;
  if (!loaded) return <Loading />;

  return (
    <section>
      <h1>{isEdit ? '합격수기 수정' : '합격수기 작성'}</h1>
      {status !== 'PUBLISHED' && (
        <>
          <HiddenNotice status={status} />
          <p>수정하면 관리자 재검토를 거친 뒤 다시 공개됩니다.</p>
        </>
      )}

      <form onSubmit={submit} noValidate>
        <div>
          {/* SearchSelect(이은아)가 input id를 받지 않아 label 연결 불가 → 문구로 표시 */}
          <span>합격한 자격증 (필수)</span>
          <SearchSelect
            placeholder="자격증 이름 검색"
            fetchOptions={fetchQualifications}
            getLabel={(item) => item.jmfldnm}
            onSelect={(item) => setForm((f) => ({ ...f, jmcd: item.jmcd, jmfldnm: item.jmfldnm }))}
          />
          {form.jmfldnm && <p>선택한 자격증: {form.jmfldnm}</p>}
          <FieldError message={fieldErrors.jmcd} />
        </div>

        <p>
          <label htmlFor="ps-body">상세 후기·실전 팁 (필수)</label>
          <textarea id="ps-body" rows={10} value={form.text} onChange={(e) => set('text')(e.target.value)} />
          <span>
            {bodyHtml.length} / {MAX_BODY}
          </span>
          <FieldError message={fieldErrors.text} />
        </p>

        <CodeSelect
          id="ps-base"
          label="응시자 베이스"
          value={form.examinee_base}
          labels={EXAMINEE_BASE_LABELS}
          onChange={set('examinee_base')}
        />
        <CodeSelect
          id="ps-period"
          label="준비 기간"
          value={form.prep_period}
          labels={PREP_PERIOD_LABELS}
          onChange={set('prep_period')}
        />
        <CodeSelect
          id="ps-attempt"
          label="응시 횟수"
          value={form.attempt_band}
          labels={ATTEMPT_LABELS}
          onChange={set('attempt_band')}
        />
        <p>
          <label htmlFor="ps-textbook">사용 교재명</label>
          <input
            id="ps-textbook"
            maxLength={50}
            value={form.textbook_name}
            onChange={(e) => set('textbook_name')(e.target.value)}
          />
        </p>
        <p>
          <label htmlFor="ps-score">합격 점수</label>
          <input
            id="ps-score"
            type="number"
            min="0"
            step="0.01"
            value={form.pass_score}
            onChange={(e) => set('pass_score')(e.target.value)}
          />
        </p>
        <p>
          <label htmlFor="ps-date">시험 응시일</label>
          <input id="ps-date" type="date" value={form.exam_date} onChange={(e) => set('exam_date')(e.target.value)} />
        </p>

        {submitError && <p className="field-error">{submitError}</p>}
        <div>
          <button type="button" className="btn" onClick={() => setConfirmCancel(true)}>
            취소
          </button>
          <button type="submit" className="btn btn--primary" disabled={submitting}>
            {isEdit ? '수정' : '등록'}
          </button>
        </div>
      </form>

      {confirmCancel && (
        <ConfirmModal
          message="작성 중인 글은 저장되지 않습니다. 나갈까요?"
          confirmText="나가기"
          onConfirm={() => navigate(-1)}
          onCancel={() => setConfirmCancel(false)}
        />
      )}
    </section>
  );
}
